uniform vec4 g_Color;

void main() {
    float alpha = 1.0 - gl_FragCoord.y / 720.0; // 假设屏幕高度为 720 像素
    gl_FragColor = vec4(g_Color.rgb, g_Color.a * alpha);
}