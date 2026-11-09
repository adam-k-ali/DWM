#version 330

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>

// Door portal composite: copies the portal FBO texel onto the doorway quad.
// Unlit on purpose: the portal scene was already lit with its own world's lightmap and face
// shading, so the entity shader's per-face light and the viewer's lightmap would darken it twice.
// Fog is kept so a distant doorway fades with the shell around it.

uniform sampler2D Sampler0;

in float sphericalVertexDistance;
in float cylindricalVertexDistance;
in vec2 texCoord0;

out vec4 fragColor;

void main() {
    vec4 color = vec4(texture(Sampler0, texCoord0).rgb, 1.0);
    fragColor = apply_fog(color, sphericalVertexDistance, cylindricalVertexDistance, FogEnvironmentalStart, FogEnvironmentalEnd, FogRenderDistanceStart, FogRenderDistanceEnd, FogColor);
}
