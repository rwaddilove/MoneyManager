// https://www.codejava.net/java-core/collections/java-list-collection-tutorial-and-examples
// boolean isDouble = Pattern.matches("^\d*$", myString);

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

class Account {
    String name;
    double startBalance;
    double balance;
    List<Object> transactions = new ArrayList<>();

    public Account(String name, double startBalance) {
        this.name = name;
        this.startBalance = startBalance;
        this.balance = startBalance;
    }
}

class Input {
    public static String InputStr(String prompt, int len) {
        System.out.print(prompt);
        Scanner input = new Scanner(System.in);
        String inp = input.nextLine().strip();
        return (inp.length() > len) ? inp.substring(0, len) : inp; }

    public static double InputDouble(String prompt) {
        try {
            return Double.parseDouble(InputStr(prompt, 10)); }
        catch (NumberFormatException e) {
            return 0.0; } }     // a value not used

    public static int InputInt(String prompt) {
        try {
            return Integer.parseInt(InputStr(prompt, 6)); }
        catch (NumberFormatException e) {
            return 9999; } }     // a value not used

    public static char InputChr(String prompt) {
        String inp = InputStr(prompt, 3).toLowerCase();
        return inp.isBlank() ? '*' : inp.charAt(0); }

    public static boolean isInteger(String s) {
        for (char c : s.toCharArray())
            if (!Character.isDigit(c)) return false;
        return true; }

    public static String InputDate(String prompt) {
        String inp = InputStr(prompt,15);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd");
        try {
            LocalDate date = LocalDate.parse(inp, formatter);}
        catch (DateTimeParseException dtpe) {
            System.out.println("Date not set (not recognised).");
            inp = ""; }
        return inp;
    }
}


class FileOp {

    public static void Read(String fp, ArrayList<ArrayList<String>> tasks) {
        tasks.clear();
        try (Scanner readFile = new Scanner(new File(fp))) {
            while (readFile.hasNextLine()) {
                String line = readFile.nextLine();
                line = line.substring(1, line.length()-1);        // strip first and last "
                String[] values = line.split("\",\"");      // split into task fields
                tasks.add(new ArrayList<>());
                for (String value : values)
                    tasks.getLast().add(value);
                if (tasks.getLast().size() < 6) tasks.getLast().add("");  // "" not added
            }
        }
        catch (FileNotFoundException e) { System.out.println("'" + fp + "' not found."); }
    }

    public static void Write(String fp, ArrayList<ArrayList<String>> tasks) {
        try (FileWriter writeFile = new FileWriter(fp)) {
            for (ArrayList<String> tsk : tasks ) {
                String line = "\"";
                for (String s : tsk)
                    line += s + "\",\"";
                writeFile.write(line.substring(0, (line.length()-2)) + "\n");
            }
        }
        catch (IOException e) { System.out.println("Could not save " + fp); }
    }
}




public class MoneyMan {
    public static void main(String[] args) {

    }
}
