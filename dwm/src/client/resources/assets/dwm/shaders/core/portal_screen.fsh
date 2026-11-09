#version 330

// Parallax door portal: the portal FBO is rendered from the viewer-relative camera at the
// main target's resolution, so each doorway fragment copies the texel at its own screen position.
// Unlit on purpose: the portal scene is already lit; re-shading it would darken the doorway.

uniform sampler2D Sampler0;

out vec4 fragColor;

void main() {
    fragColor = vec4(texelFetch(Sampler0, ivec2(gl_FragCoord.xy), 0).rgb, 1.0);
}
