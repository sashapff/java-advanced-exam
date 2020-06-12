package ru.ifmo.rain.ivanova;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

public class Translate {
    private static final Tree tree = new Tree();

    private static class Node {
        final Map<String, Node> map = new HashMap<>();
        Optional<String> translation = Optional.empty();
    }

    private static class Tree {
        final Node root = new Node();

        void add(final String expression, final String translation) {
            final String[] words = expression.toLowerCase().split(" ");
            Node current = root;
            for (String word : words) {
                if (current.map.containsKey(word)) {
                    current = current.map.get(word);
                } else {
                    final Node newNode = new Node();
                    current.map.put(word, newNode);
                    current = newNode;
                }
            }
            current.translation = Optional.of(translation);
        }

        void translate(final WordReader reader, final BufferedWriter writer) throws IOException {
            while (reader.hasNext()) {
                Node current = root;
                Node success = null;
                Deque<String> queue = new ArrayDeque<>();
                while (reader.hasNext()) {
                    final String word = reader.nextWord();
                    queue.addLast(word);
                    if (current.map.containsKey(word)) {
                        current = current.map.get(word);
                        if (current.translation.isPresent()) {
                            success = current;
                            queue.clear();
                        }
                    } else {
                        break;
                    }
                }
                if (success == null) {
                    writer.write(queue.pollFirst() + " ");
                } else {
                    writer.write(success.translation.get() + " ");
                }
                while (!queue.isEmpty()) {
                    reader.putWord(queue.pollLast());
                }
            }
        }
    }

    private static void parseDictionary(final BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] s = line.split(Pattern.quote(" | "));
            if (s.length != 2 || Arrays.stream(s).anyMatch(Objects::isNull)) {
                throw new IllegalArgumentException("Invalid dictionary definition: " + line);
            }
            tree.add(s[0], s[1]);
        }
    }

    private static class WordReader {
        Deque<String> queue = new ArrayDeque<>();
        final BufferedReader reader;

        WordReader(final BufferedReader reader) {
            this.reader = reader;
        }

        String nextWord() throws IOException {
            String line;
            while (queue.isEmpty() && (line = reader.readLine()) != null) {
                queue.addAll(Arrays.asList(line.toLowerCase().split(" ")));
            }
            return queue.pollFirst();
        }

        void putWord(final String s) {
            queue.addFirst(s);
        }

        boolean hasNext() throws IOException {
            String line;
            while (queue.isEmpty() && (line = reader.readLine()) != null) {
                queue.addAll(Arrays.asList(line.toLowerCase().split(" ")));
            }
            return !queue.isEmpty();
        }

    }

    static void translate(final BufferedReader reader, final BufferedReader readerDir, final BufferedWriter writer) throws IOException {
        parseDictionary(readerDir);
        tree.translate(new WordReader(reader), writer);
    }

    public static void main(String[] args) {
        if (args == null || args.length != 3 || Arrays.stream(args).anyMatch(Objects::isNull)) {
            System.err.println("Incorrect arguments");
            System.err.println("Enter <input file> <dictionary file> <output file>");
            return;
        }
        try (
                final BufferedReader reader = Files.newBufferedReader(Paths.get(args[0]));
                final BufferedReader readerDir = Files.newBufferedReader(Paths.get(args[1]));
                final BufferedWriter writer = Files.newBufferedWriter(Paths.get(args[2]))
        ) {
            translate(reader, readerDir, writer);
        } catch (IOException e) {
            System.err.println("I/O exception occurred: " + e);
        }
    }
}
