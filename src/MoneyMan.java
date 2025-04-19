// https://www.codejava.net/java-core/collections/java-list-collection-tutorial-and-examples
// boolean isDouble = Pattern.matches("^\d*$", myString);

import java.io.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

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
            return 0.0; } }

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


class BankAccount {
    String name;
    double balance;
    ArrayList<ArrayList<String>> transactions = new ArrayList<>();

    public BankAccount(String name, double startBalance) {
        this.name = name;
        this.balance = startBalance; }

    public void ShowTransactions() {
        if (transactions.isEmpty()) {
            System.out.println("No transactions.");
            return; }
        System.out.println("Transactions for account: " + name);
        int i = 0;
        for (ArrayList<String> item : transactions) {
            System.out.printf("%2d %-18s %-10s %10s %-10s %-3s\n", i++, item.get(0), item.get(1), item.get(2), item.get(3), item.get(4));
        }
    }

    public void AddTransaction() {
        // payee, date, amount, category, notes, unused
        System.out.println("ADD TRANSACTION");
        String payee = Input.InputStr("Payee: ", 20);
        String date = Input.InputStr("Date: ", 20);
        Double amount = Input.InputDouble("Amount: ");
        String category = Input.InputStr("Category: ", 20);
        String notes = Input.InputStr("notes: ", 20);
        transactions.add(new ArrayList<>());
        Collections.addAll(transactions.getLast(), Double.toString(amount), date, category, notes, "*");
//        transactions.getLast().add(payee);
//        transactions.getLast().add(Double.toString(amount));
//        transactions.getLast().add(date);
//        transactions.getLast().add(category);
//        transactions.getLast().add(notes);
//        transactions.getLast().add("*");
    }
}


class General {     // general functions not specific to any account

    public static void AddAccount(List<BankAccount> accounts) {
        String name = Input.InputStr("ADD NEW ACCOUNT:\nEnter name: ", 10);
        double openingBalance = Input.InputDouble("Enter opening balance: ");
        accounts.add(new BankAccount(name, openingBalance));
    }

    public static void ListAccounts(List<BankAccount> accounts) {
        System.out.println("BANK ACCOUNTS:");
        for (int i = 0; i < accounts.size(); ++i)
            System.out.println(i + " Name: " + accounts.get(i).name + " Balance: " + accounts.get(i).balance);
    }
}

public class MoneyMan {
    public static void main(String[] args) {
        List<BankAccount> accounts = new ArrayList<>();
        int currentAccount = 9999;   // any invalid number
        String cmd = "";

        while (!cmd.equals("quit")) {
            General.ListAccounts(accounts);
            cmd = Input.InputStr("\nEnter account num, Add or Quit: ", 5).toLowerCase();
            if (cmd.equals("add")) General.AddAccount(accounts);
            if (Input.isInteger(cmd)) currentAccount = Integer.parseInt(cmd);   // select an account

            while (currentAccount < accounts.size()) {      // while an account is selected
                accounts.get(currentAccount).ShowTransactions();
                cmd = Input.InputStr("\nAdd or Close: ", 5).toLowerCase();
                if (cmd.equals("add")) accounts.get(currentAccount).AddTransaction();
                if (cmd.equals("close")) currentAccount = 9999;
            }
        }

    }
}
