package com.fmoreno.telegramtaskaiagent.util;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class AudioConverter {

    public File convertToWav(File inputFile) throws IOException, UnsupportedAudioFileException {
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(inputFile);
        AudioFormat baseFormat = audioInputStream.getFormat();
        AudioFormat targetFormat = new AudioFormat(
                AudioFormat.Encoding.PCM_SIGNED,
                baseFormat.getSampleRate(),
                16,
                baseFormat.getChannels(),
                baseFormat.getChannels() * 2,
                baseFormat.getSampleRate(),
                false
        );
        AudioInputStream convertedInputStream = AudioSystem.getAudioInputStream(targetFormat, audioInputStream);
        File outputFile = new File(inputFile.getParent(), "converted.wav");
        AudioSystem.write(convertedInputStream, AudioFileFormat.Type.WAVE, outputFile);
        return outputFile;
    }
}
