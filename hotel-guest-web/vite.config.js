import { resolve } from "node:path";
import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

export default defineConfig({
  base: "/guest/",
  plugins: [vue()],
  server: {
    host: "0.0.0.0",
    port: 5173
  },
  build: {
    outDir: resolve(__dirname, "../sky-take-out/sky-server/src/main/resources/guest"),
    emptyOutDir: true
  }
});
