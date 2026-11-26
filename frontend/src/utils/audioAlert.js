// Web Audio API Synthesized Audio Alarm & Reminder Beep Engine
// Uses the browser's built-in AudioContext for 100% reliable sound playback without external audio files

export function playReminderBeep() {
  try {
    const AudioContext = window.AudioContext || window.webkitAudioContext;
    if (!AudioContext) return;
    const ctx = new AudioContext();

    if (ctx.state === 'suspended') {
      ctx.resume();
    }

    const playTone = (freq, startTime, duration, type = 'sine') => {
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();

      osc.type = type;
      osc.frequency.setValueAtTime(freq, startTime);

      // Smooth envelope attack and decay
      gain.gain.setValueAtTime(0.001, startTime);
      gain.gain.linearRampToValueAtTime(0.35, startTime + 0.04);
      gain.gain.exponentialRampToValueAtTime(0.001, startTime + duration);

      osc.connect(gain);
      gain.connect(ctx.destination);

      osc.start(startTime);
      osc.stop(startTime + duration);
    };

    const now = ctx.currentTime;
    // Pleasant, attention-grabbing medical chime (C6 -> E6 -> G6 -> C7 arpeggio)
    playTone(1046.5, now, 0.18, 'sine');          // C6
    playTone(1318.5, now + 0.20, 0.18, 'sine');   // E6
    playTone(1567.98, now + 0.40, 0.22, 'sine');  // G6
    playTone(2093.0, now + 0.65, 0.45, 'sine');   // C7 (Harmonic peak)
  } catch (err) {
    console.warn('AudioContext playback error (user interaction may be required)', err);
  }
}

export function playUrgentBeep() {
  try {
    const AudioContext = window.AudioContext || window.webkitAudioContext;
    if (!AudioContext) return;
    const ctx = new AudioContext();

    if (ctx.state === 'suspended') {
      ctx.resume();
    }

    const beep = (startTime) => {
      const osc = ctx.createOscillator();
      const gain = ctx.createGain();

      osc.type = 'triangle';
      osc.frequency.setValueAtTime(987.77, startTime); // B5 medical frequency

      gain.gain.setValueAtTime(0.001, startTime);
      gain.gain.linearRampToValueAtTime(0.4, startTime + 0.02);
      gain.gain.exponentialRampToValueAtTime(0.001, startTime + 0.22);

      osc.connect(gain);
      gain.connect(ctx.destination);

      osc.start(startTime);
      osc.stop(startTime + 0.24);
    };

    const now = ctx.currentTime;
    beep(now);
    beep(now + 0.28);
    beep(now + 0.56);
  } catch (err) {
    console.warn('Urgent beep error', err);
  }
}
