#include <jni.h>
#include <android/log.h>
#include <string>
#include <vector>
#include "llama.h"

#define TAG "LlamaJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_example_screenagent_llm_LlamaBridge_initContext(JNIEnv *env, jobject, jstring mp) {
    const char *p = env->GetStringUTFChars(mp, nullptr);
    LOGI("Loading model: %s", p);
    llama_backend_init();
    llama_model_params mparams = llama_model_default_params();
    mparams.n_gpu_layers = 0;
    llama_model *m = llama_model_load_from_file(p, mparams);
    env->ReleaseStringUTFChars(mp, p);
    if (!m) { LOGE("model load failed"); return 0; }
    llama_context_params cp = llama_context_default_params();
    cp.n_ctx = 2048;
    cp.n_threads = 4;
    cp.n_batch = 256;
    llama_context *ctx = llama_init_from_model(m, cp);
    if (!ctx) { llama_model_free(m); LOGE("context init failed"); return 0; }
    return reinterpret_cast<jlong>(ctx);
}

JNIEXPORT jstring JNICALL
Java_com_example_screenagent_llm_LlamaBridge_generate(JNIEnv *env, jobject, jlong ptr, jstring jp, jint mx) {
    auto *ctx = reinterpret_cast<llama_context *>(ptr);
    if (!ctx) return env->NewStringUTF("");
    llama_model *m = llama_get_model(ctx);
    const llama_vocab *v = llama_model_get_vocab(m);

    const char *ps = env->GetStringUTFChars(jp, nullptr);
    std::string in(ps);
    env->ReleaseStringUTFChars(jp, ps);

    int n = -llama_tokenize(v, in.c_str(), in.size(), nullptr, 0, true, true);
    if (n <= 0) return env->NewStringUTF("");
    std::vector<llama_token> toks(n);
    llama_tokenize(v, in.c_str(), in.size(), toks.data(), n, true, true);

    llama_batch b = llama_batch_get_one(toks.data(), toks.size());
    if (llama_decode(ctx, b) != 0) return env->NewStringUTF("");

    std::string out;
    for (int i = 0; i < mx; i++) {
        float *lg = llama_get_logits_ith(ctx, -1);
        int nv = llama_vocab_n_tokens(v);
        llama_token id = 0;
        float mxl = lg[0];
        for (int j = 1; j < nv; j++) {
            if (lg[j] > mxl) { mxl = lg[j]; id = j; }
        }
        if (llama_vocab_is_eog(v, id)) break;
        char buf[256];
        int k = llama_token_to_piece(v, id, buf, sizeof(buf), 0, true);
        if (k > 0) out.append(buf, k);
        llama_batch nb = llama_batch_get_one(&id, 1);
        if (llama_decode(ctx, nb) != 0) break;
    }
    return env->NewStringUTF(out.c_str());
}

JNIEXPORT void JNICALL
Java_com_example_screenagent_llm_LlamaBridge_freeContext(JNIEnv *, jobject, jlong ptr) {
    auto *ctx = reinterpret_cast<llama_context *>(ptr);
    if (ctx) {
        llama_model *m = llama_get_model(ctx);
        llama_free(ctx);
        llama_model_free(m);
    }
}

}
