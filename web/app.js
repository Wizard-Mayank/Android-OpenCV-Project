"use strict";
document.addEventListener('DOMContentLoaded', () => {
    const resolutionEl = document.getElementById('resolution');
    const fpsEl = document.getElementById('fps');
    if (resolutionEl) {
        resolutionEl.textContent = '640x480';
    }
    if (fpsEl) {
        fpsEl.textContent = '15 FPS';
    }
});
