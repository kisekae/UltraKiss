/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.wmiles.kisekaeultrakiss.Kisekae;

/**
 *
 * @author william
 */
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

public class AudioConverter {

    /**
     * Converts an audio file from one format (e.g., AU) to WAV format.
     *
     * @param inputStream The original AU file.
     * @param destinationFile The target WAV file.
     * @throws UnsupportedAudioFileException if the source file format is not supported.
     * @throws IOException if an I/O exception occurs during reading or writing.
     */
    public static void convertAuToWav(InputStream inputStream, File destinationFile) 
            throws UnsupportedAudioFileException, IOException {

        try (AudioInputStream auStream = AudioSystem.getAudioInputStream(inputStream)) {
            // Check if the system can write the audio data to a WAV file type
            if (!AudioSystem.isFileTypeSupported(AudioFileFormat.Type.WAVE, auStream)) {
                // In some cases, an intermediate conversion to a standard PCM format 
                // might be necessary if the original AU format is unusual.
                // However, the AudioSystem.write method often handles direct conversion 
                // if the codecs are available. The base API typically supports AU, AIFF, and WAV.

                // If direct conversion fails, you can attempt to convert to a PCM stream first:
                // AudioFormat pcmFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 
                //     auStream.getFormat().getSampleRate(), 16, auStream.getFormat().getChannels(),
                //     auStream.getFormat().getFrameSize() * 2, auStream.getFormat().getFrameRate(), false);
                // try (AudioInputStream pcmStream = AudioSystem.getAudioInputStream(pcmFormat, auStream)) {
                //     AudioSystem.write(pcmStream, AudioFileFormat.Type.WAVE, destinationFile);
                // }
                
                // For typical AU files, the direct approach below should work.
                System.out.println("Direct WAV conversion not officially supported for this specific AU format, but attempting...");
            }
            
            // Write the AudioInputStream to a WAV file
            AudioSystem.write(auStream, AudioFileFormat.Type.WAVE, destinationFile);
            System.out.println("Conversion successful: " + destinationFile.getAbsolutePath());

        } catch (IllegalArgumentException e) {
            System.err.println("Conversion failed: The system does not support writing to WAV format with the given audio input stream.");
            e.printStackTrace();
        }
    }
}