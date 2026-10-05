# Third-party software and model notices

## llama.cpp

The Android native runtime builds llama.cpp from source at commit
`2e7c58c5477478c8cf6e199cfaa5dcd5a4319c81` using CMake FetchContent.
The upstream project is MIT licensed: <https://github.com/ggml-org/llama.cpp>.

MIT License

Copyright (c) 2023-2026 ggml authors

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

## Optional Qwen model

The optional `Qwen3-0.6B-Q8_0.gguf` model is published by Qwen under Apache-2.0:
<https://huggingface.co/Qwen/Qwen3-0.6B-GGUF>. It is not bundled with the app.
The app imports it only after matching the pinned SHA-256 in
`model-manifest.json`. Model use remains subject to the upstream license and
model terms; retain the upstream notices when redistributing model weights.
