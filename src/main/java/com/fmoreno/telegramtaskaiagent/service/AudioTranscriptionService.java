package com.fmoreno.telegramtaskaiagent.service;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.LiveSpeechRecognizer;
import edu.cmu.sphinx.api.SpeechResult;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Log4j2
@Service
public class AudioTranscriptionService {

    private final LiveSpeechRecognizer recognizer;

    public AudioTranscriptionService() throws IOException {
        Configuration configuration = new Configuration();

        // Set path to the acoustic model.
        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");

        // Set path to the dictionary.
        configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");

        // Set path to the language model.
        configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");

        recognizer = new LiveSpeechRecognizer(configuration);
    }

    public String transcribeAudio(File audioFile) {
        try {
            recognizer.startRecognition(audioFile);
            SpeechResult result;
            StringBuilder transcription = new StringBuilder();
            while ((result = recognizer.getResult()) != null) {
                transcription.append(result.getHypothesis()).append(" ");
            }
            recognizer.stopRecognition();
            return transcription.toString().trim();
        } catch (IOException e) {
            log.error("Error during audio transcription", e);
            return "An error occurred during audio transcription. Please try again.";
        }
    }
}
