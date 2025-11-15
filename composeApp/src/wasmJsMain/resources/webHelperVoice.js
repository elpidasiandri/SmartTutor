export function speakText(text) {
    window.speechSynthesis.speak(new SpeechSynthesisUtterance(text));
}

export let recognition = null;

export function startVoiceRecognition(callback) {
    recognition = new (window.SpeechRecognition || window.webkitSpeechRecognition)();
    recognition.continuous = false;
    recognition.interimResults = false;
    recognition.lang = "en-US";
    recognition.result = function(event) {
        callback(event.results[0][0].transcript);
    };
    recognition.error = function(event) {
        console.log("Speech recognition error: " + event.error);
    };
    recognition.start();
}

export function stopVoiceRecognition() {
    if (recognition) recognition.stop();
    recognition = null;
}
