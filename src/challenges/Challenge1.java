package challenges;

/*
 * Made for the CodingHelp coding challenge in May 2021.<br>
 * This work is licensed under the Apache License v2.0 <br>
 * Apache © 2021 OpenSrcerer
 */

import java.io.*;
import java.util.LinkedList;
import java.util.stream.Collector;
import java.util.stream.Stream;

/**
 * Challenge 1 - Demonstrating reversing a string using the Java Streams API and Linked Lists.
 */
public class Challenge1 {
    public static void main(String[] args) {
        try ( /* Initialize an input and output stream, using wrappers for a custom usage */
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                PrintStream out = new PrintStream(new BufferedOutputStream(System.out))
        ) {
            out.println("Insert the word you would like to reverse:"); // Prompt user for input
            out.flush();

            String wordToReverse = in.readLine(); // Store the user's input.

            String reversed = reverse(wordToReverse.chars().mapToObj(x -> (char) x)) // Grab a reversed stream of the char array
                    .collect(Collector.of( // Collect the reversed word back to a string
                            StringBuilder::new,
                            StringBuilder::append,
                            StringBuilder::append,
                            StringBuilder::toString)
                    );

            out.println("Your reversed word is: " + reversed);
            out.flush();
        } catch (IOException ex) {
            System.out.println("Uh oh, looks like something went very wrong. But no no, stay calm.");
        }
    }

    /**
     * Takes a stream in and reverses it using the means of a Linked List.
     * @param stream The stream to reverse.
     * @param <X> The generic type of the stream to take in.
     * @return A Stream identical to the original one with a reversed order.
     */
    public static <X> Stream<X> reverse(Stream<X> stream) {
        LinkedList<X> linkedWord = new LinkedList<>();
        stream.forEach(linkedWord::push);
        return linkedWord.stream();
    }
}