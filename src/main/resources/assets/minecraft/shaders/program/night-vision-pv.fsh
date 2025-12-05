#version 120

uniform float NightVisionEnabled;

uniform float VignetteEnabled;

uniform float VignetteRadius;

uniform float Brightness;

uniform float SepiaRatio;

uniform sampler2D DiffuseSampler;

uniform sampler2D NoiseSampler;

uniform float Time;

varying vec2 texCoord;
varying vec2 oneTexel;
varying vec4 outPos;

uniform vec2 InSize;

uniform float NoiseAmplification;

uniform float IntensityAdjust;

//const float RADIUS = 0.55;

const float SOFTNESS = 0.38;

const float contrast = 0.8;

const vec3 SEPIA = vec3(1.2, 1.0, 0.8);

void main() {
    vec4 texColor = texture2D(DiffuseSampler, texCoord.xy);
    
    texColor.rgb *= Brightness;
    
if(NightVisionEnabled > 0) {

    // ---- 原噪点系统仍然保留（保持效果中有些 "相机噪点" ）----
    vec2 uv;
    uv.x = 0.35 * sin(Time * 10);
    uv.y = 0.35 * cos(Time * 10);
    vec3 noise = texture2D(NoiseSampler, texCoord.xy + uv).rgb * NoiseAmplification;
    texColor.xy += noise.xy * 0.005;

    // ---- 黑白灰度 ----
    float gray = dot(texColor.rgb, vec3(0.299, 0.587, 0.114));
    gray = contrast * (gray - 0.5) + 0.5;

    // ---- ⭐ 对比度增强（可调） ⭐ ----
    float contrast = 1; // <<<<<< 在这里调高或调低
    gray = contrast * (gray - 0.5) + 0.5;
    gray = clamp(gray, 0.0, 1.0);

    // ---- 输出黑白 ----
    texColor = vec4(gray, gray, gray, 1.0);
}


    if(VignetteEnabled > 0) {
        float dist = distance(texCoord.xy, vec2(0.5,0.5));
        float vignette = smoothstep(VignetteRadius, VignetteRadius - SOFTNESS, dist);
        texColor.rgb *= vignette;
        texColor.a = 1.0;
    }

    if(SepiaRatio > 0) {
        float gray = dot(texColor.rgb, vec3(0.299, 0.587, 0.114));
        vec4 sepiaColor = vec4(vec3(gray) * SEPIA, 1.0);
        texColor = mix(texColor, sepiaColor, SepiaRatio);
    }
    
    
    //gl_FragColor = texColor; Causes horizontal artifacts in 1.7.10
    
    gl_FragColor = vec4(texColor.rgb, 1);
}