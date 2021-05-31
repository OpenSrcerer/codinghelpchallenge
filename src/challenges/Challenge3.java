package challenges;

/*
 * Made for the CodingHelp coding challenge in May 2021.<br>
 * This work is licensed under the Apache License v2.0 <br>
 * Apache © 2021 OpenSrcerer
 */

import java.io.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.LongStream;

/**
 * Challenge 3 - Demonstrating the commonly known FizzBuzz challenge using multithreading.
 * This program allows you to run FizzBuzz and count up to 9,223,372,036,854,775,807.
 *
 * The main purpose of it though is to demonstrate how multithreading can improve performance.
 * By running the program in multi-threaded mode, you are able to view the difference in time between a single-threaded
 * and multi-threaded approach.
 * You can also save these results to a file.
 */
public class Challenge3 {

    /**
     * The number of cores available in the user's system.
     */
    private static int nCores;

    /**
     * The pool of threads to use if program will run in a multithreaded context.
     */
    private static ExecutorService service = null;

    /**
     * List of futures that will be used in a multithreading context.
     */
    private static final List<Future<List<Serializable>>> futures = new ArrayList<>();

    /**
     * The output to print to a file.
     */
    private static final List<String> output = new ArrayList<>();

    /**
     * The number that FizzBuzzSuite will run to. (needs to be a positive integer)
     */
    private static long runningNumber;

    public static void main(String[] args) throws IOException {
        try ( /* Custom input and output streams. The output stream is more performant in a MT context */
                BufferedReader in = new BufferedReader(new InputStreamReader(new BufferedInputStream(System.in)));
                PrintStream out = new PrintStream(new BufferedOutputStream(System.out), true)
        ) {
            out.println("""
                      
                      ______   _               ____                     \s
                     |  ____| (_)             |  _ \\                    \s
                     | |__     _   ____  ____ | |_) |  _   _   ____  ____
                     |  __|   | | |_  / |_  / |  _ <  | | | | |_  / |_  /
                     | |      | |  / /   / /  | |_) | | |_| |  / /   / /\s
                     |_|      |_| /___| /___| |____/   \\__,_| /___| /___|
                                                                        \s
                                _____           _   _         \s
                               / ____|         (_) | |        \s
                              | (___    _   _   _  | |_    ___\s
                               \\___ \\  | | | | | | | __|  / _ \\
                               ____) | | |_| | | | | |_  |  __/
                              |_____/   \\__,_| |_|  \\__|  \\___|
                                                              \s
                                 Made by <@178603029115830282>
                    """);

            String retry = "Y";
            while (retry.equals("Y") || retry.equals("y")) {
                nCores = Runtime.getRuntime().availableProcessors(); // Reset core count
                futures.clear();

                out.print("Insert the number that FizzBuzz should run to: "); // Prompt user for input

                String input = in.readLine();
                runningNumber = Long.parseLong(input); // Store up to what FizzBuzz should run
                if (runningNumber <= 0) {
                    throw new NumberFormatException(); // Deny negative or zero numbers
                }

                if (runningNumber < nCores) { // If the given value to run fizzbuzz to is lesser than the number of cores
                    nCores -= (nCores - runningNumber); // Reduce the number of cores by the difference
                }

                out.print("Should FizzBuzzSuite run in a multithreaded context? (Y/n): ");
                input = in.readLine();
                boolean multiThreaded = !input.equals("n") && !input.equals("N");

                boolean threadView = true;
                if (multiThreaded) {
                    if (service == null) {
                        service = Executors.newFixedThreadPool(nCores);
                    }
                    out.print("Should FizzBuzzSuite show a thread-context view? (Y/n): ");
                    input = in.readLine();
                    threadView = !input.equals("n") && !input.equals("N");
                }

                { // Empty scope to erase timers from memory
                    Instant timeStart = Instant.now(); // Start a stopwatch
                    if (multiThreaded) {
                        multiThreadedFizzBuzz(out, threadView); // Run FizzBuzz in multithreaded mode
                    } else {
                        nCores = 1;
                        sequentialFizzBuzz(out); // Run FizzBuzz in sequential mode
                    }
                    Instant timeEnd = Instant.now(); // End the stopwatch

                    String timeTaken = ChronoUnit.MILLIS.between(timeStart, timeEnd) + "ms";
                    out.println("Total time taken: " + timeTaken);
                    output.add("Counted to: " + runningNumber + " // Threads: " + nCores + " // Time Taken: " + timeTaken);
                }

                out.println();
                out.print("Re-run a FizzBuzz test? (y/N): ");
                retry = in.readLine();
            }

            if (service != null) {
                service.shutdown();
            }

            out.println("Save temporal results to a file? (y/N): ");
            String save = in.readLine();

            if (save.equals("Y") || save.equals("y")) {
                File outputFile = new File("FizzBuzz-" + System.currentTimeMillis() + ".txt");
                if (outputFile.createNewFile()) {
                    try (FileWriter writer = new FileWriter(outputFile)) {
                        for (String s : output) {
                            writer.write(s + "\n"); // Write results to file
                        }
                    }
                    out.println("Saved data to: " + outputFile.getAbsolutePath());
                }
            }

            out.println("Thank you for using FizzBuzz suite!");
        } catch (NumberFormatException ex) {
            System.out.println("The value you've entered seems to not be a valid positive integer. Please try again.");
        }
    }

    /**
     * Run FizzBuzz in multi threaded mode
     * @param out Output PrintStream
     * @param threadView Whether the output should be viewed in a thread context
     */
    private static void multiThreadedFizzBuzz(PrintStream out, boolean threadView) {
        out.println("""
                    ╭―――――――――――――――――――╮
                    │                               │
                    │    MULTITHREADED FIZZBUZZ     │
                    │                               │
                    ╰―――――――――――――――――――╯
                    """);

        final long partition = runningNumber / nCores; // Split the number into a partition that can be taken by each thread
        final long extra = runningNumber - partition * nCores; // Find the remaining truncated value

        for (int thread = 0; thread < nCores; ++thread) {
            final long start = thread * partition + 1; // Where each thread should start
            final long end = (thread + 1) * partition; // Where each thread should end
            final long currThread = thread; // Effectively final thread value to pass in the lambda below

            futures.add(service.submit(() -> {
                List<Serializable> toPrint = new ArrayList<>();
                LongStream.rangeClosed(start, currThread == nCores - 1 ? end + extra : end)
                        /* Breaking down this one-liner for you:
                        If the current number is divisible by three, check if it's divisible by 5 too. If it is: Fizzbuzz!
                        Otherwise, Fizz!.
                        If the number is not divisible by three, is it divisible by 5? If yes, Buzz!
                        Otherwise, print the number. */
                        .mapToObj(index -> index % 3 == 0 ? (index % 5 == 0 ? "FizzBuzz!" : "Fizz!") : (index % 5 == 0 ? "Buzz!" : index))
                        .forEach(toPrint::add); // Add what to print in a list
                return toPrint;
            }));
        }

        try {
            for (int index = 0; index < nCores; ++index) {
                if (threadView) {
                    out.println("Thread-" + (index + 1));
                    out.println(futures.get(index).get().toString());
                    out.println("-------------------\n");
                } else {
                    futures.get(index).get().forEach(s -> System.out.print(s + " - "));
                    out.println();
                }
            }
        } catch (ExecutionException | InterruptedException ex) {
            out.println("The calculation is exceptional or one of the working threads may have been interrupted.");
        }
    }

    /**
     * Run FizzBuzz suite in sequential mode.
     * @param out Output PrintStream
     */
    private static void sequentialFizzBuzz(PrintStream out) {
        out.println("""
                    ╭―――――――――――――――――――╮
                    │                               │
                    │      SEQUENTIAL FIZZBUZZ      │
                    │                               │
                    ╰―――――――――――――――――――╯
                    """);

        LongStream.rangeClosed(1, runningNumber)
                /* Breaking down this one-liner for you:
                If the current number is divisible by three, check if it's divisible by 5 too. If it is: Fizzbuzz!
                Otherwise, Fizz!.
                If the number is not divisible by three, is it divisible by 5? If yes, Buzz!
                Otherwise, print the number. */
                .mapToObj(index -> index % 3 == 0 ? (index % 5 == 0 ? "FizzBuzz!" : "Fizz!") : (index % 5 == 0 ? "Buzz!" : index))
                .forEach(s -> System.out.print(s + " - ")); // Add what to print in a list
        out.println();
    }
}
