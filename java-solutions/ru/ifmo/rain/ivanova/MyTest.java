package ru.ifmo.rain.ivanova;

import org.junit.Test;

import java.io.*;

import static org.junit.Assert.assertEquals;

public class MyTest {
    public String test(final String input, final String dictionary) throws IOException {
        final BufferedReader reader = new BufferedReader(new StringReader(input));
        final BufferedReader readerDir = new BufferedReader(new StringReader(dictionary));
        final StringWriter writer = new StringWriter();
        final BufferedWriter wr = new BufferedWriter(writer);
        Translate.translate(reader, readerDir, wr);
        wr.flush();
        return writer.toString();
    }

    @Test
    public void test() throws IOException {
        assertEquals("z lol ", test("kek pff kek pff kek",
                "kek | lol\n" +
                        "pff | qwertyu\n" +
                        "kek pff | as\n" +
                        "kek pff kek pff | z"));
    }

}
