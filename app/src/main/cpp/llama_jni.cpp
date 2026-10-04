#include <jni.h>
#include <android/log.h>
#include <llama.h>

#include <algorithm>
#include <mutex>
#include <string>
#include <vector>

namespace {
constexpr const char* kTag = "LiaLlama";
std::mutex g_mutex;
llama_model* g_model = nullptr;
llama_context* g_context = nullptr;

void clear_runtime() {
    if (g_context != nullptr) {
        llama_free(g_context);
        g_context = nullptr;
    }
    if (g_model != nullptr) {
        llama_model_free(g_model);
        g_model = nullptr;
    }
}

std::string from_jstring(JNIEnv* env, jstring value) {
    if (value == nullptr) return {};
    const char* chars = env->GetStringUTFChars(value, nullptr);
    if (chars == nullptr) return {};
    std::string result(chars);
    env->ReleaseStringUTFChars(value, chars);
    return result;
}
}

extern "C" JNIEXPORT jboolean JNICALL
Java_de_wea200675_lia_core_SystemJniInferenceBridge_nativeLoad(
        JNIEnv* env, jobject, jstring model_path, jstring model_id, jint threads) {
    std::lock_guard<std::mutex> lock(g_mutex);
    clear_runtime();
    const std::string path = from_jstring(env, model_path);
    const std::string id = from_jstring(env, model_id);
    if (path.empty() || id.empty()) return JNI_FALSE;

    llama_backend_init();
    llama_model_params model_params = llama_model_default_params();
    model_params.use_mmap = true;
    g_model = llama_model_load_from_file(path.c_str(), model_params);
    if (g_model == nullptr) {
        __android_log_print(ANDROID_LOG_ERROR, kTag, "Model load failed: %s", id.c_str());
        return JNI_FALSE;
    }

    llama_context_params context_params = llama_context_default_params();
    context_params.n_ctx = 2048;
    context_params.n_threads = std::max(1, static_cast<int>(threads));
    context_params.n_threads_batch = std::max(1, static_cast<int>(threads));
    context_params.n_batch = 512;
    g_context = llama_init_from_model(g_model, context_params);
    if (g_context == nullptr) {
        __android_log_print(ANDROID_LOG_ERROR, kTag, "Context initialization failed");
        clear_runtime();
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

extern "C" JNIEXPORT jstring JNICALL
Java_de_wea200675_lia_core_SystemJniInferenceBridge_nativeGenerate(
        JNIEnv* env, jobject, jstring prompt, jint max_tokens) {
    std::lock_guard<std::mutex> lock(g_mutex);
    if (g_model == nullptr || g_context == nullptr) {
        return env->NewStringUTF("");
    }
    const std::string user_text = from_jstring(env, prompt);
    if (user_text.empty()) return env->NewStringUTF("");

    const std::string prompt_text =
        "<|im_start|>system\n"
        "Antworte klar, kurz und ehrlich. Erfinde keine persönlichen Erinnerungen.\n"
        "Antworte klar, kurz und ehrlich. Erfinde keine persönlichen Erinnerungen.\\n"
        "<|im_end|>\n<|im_start|>user\n" + user_text +
        "\n<|im_end|>\n<|im_start|>assistant\n/no_think\n";
    const llama_vocab* vocab = llama_model_get_vocab(g_model);
    const int token_count = -llama_tokenize(
        vocab, prompt_text.c_str(), static_cast<int32_t>(prompt_text.size()),
        nullptr, 0, true, true);
    if (token_count <= 0 || token_count > 4096) return env->NewStringUTF("");
    std::vector<llama_token> tokens(static_cast<size_t>(token_count));
    const int tokenized = llama_tokenize(
        vocab, prompt_text.c_str(), static_cast<int32_t>(prompt_text.size()),
        tokens.data(), token_count, true, true);
    if (tokenized < 0) return env->NewStringUTF("");
    tokens.resize(static_cast<size_t>(tokenized));

    llama_memory_clear(llama_get_memory(g_context), true);
    llama_batch batch = llama_batch_init(512, 0, 1);
    std::string result;
    const int32_t bounded_max = std::clamp(static_cast<int32_t>(max_tokens), 1, 512);
    int32_t generated = 0;
    bool failed = false;
    for (size_t offset = 0; offset < tokens.size(); offset += 512) {
        const size_t count = std::min<size_t>(512, tokens.size() - offset);
        batch.n_tokens = static_cast<int32_t>(count);
        for (size_t i = 0; i < count; ++i) {
            batch.token[i] = tokens[offset + i];
            batch.pos[i] = static_cast<llama_pos>(offset + i);
            batch.n_seq_id[i] = 1;
            batch.seq_id[i][0] = 0;
            batch.logits[i] = (offset + i + 1 == tokens.size());
        }
        if (llama_decode(g_context, batch) != 0) { failed = true; break; }
    }

    llama_sampler* sampler = llama_sampler_chain_init(llama_sampler_chain_default_params());
    llama_sampler_chain_add(sampler, llama_sampler_init_greedy());
    while (!failed && generated < bounded_max) {
        const llama_token token = llama_sampler_sample(sampler, g_context, -1);
        if (llama_vocab_is_eog(vocab, token)) break;
        char piece[4096];
        const int piece_size = llama_token_to_piece(vocab, token, piece, sizeof(piece), 0, true);
        if (piece_size < 0) { failed = true; break; }
        result.append(piece, static_cast<size_t>(piece_size));
        ++generated;

        batch.n_tokens = 1;
        batch.token[0] = token;
        batch.pos[0] = static_cast<llama_pos>(tokens.size() + generated - 1);
        batch.n_seq_id[0] = 1;
        batch.seq_id[0][0] = 0;
        batch.logits[0] = true;
        if (llama_decode(g_context, batch) != 0) { failed = true; break; }
    }
    llama_sampler_free(sampler);
    llama_batch_free(batch);
    if (failed) return env->NewStringUTF("");
    return env->NewStringUTF(result.c_str());
}

extern "C" JNIEXPORT void JNICALL
Java_de_wea200675_lia_core_SystemJniInferenceBridge_nativeClose(JNIEnv*, jobject) {
    std::lock_guard<std::mutex> lock(g_mutex);
    clear_runtime();
}
