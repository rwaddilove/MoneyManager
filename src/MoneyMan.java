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
            LocalDate date = LocalDate.parse(inp, formatter); }
        catch (DateTimeParseException dtpe) {
            System.out.println("Date not set (not recognised).");
            inp = ""; }
        return inp; }
}


class FileOp {

    public static void Read(String filePath, List<BankAccount> accounts) {
        // create bank accounts, add filenames to accountFiles
        int n = 0;
        while (true) {
            String fname = filePath + "MoneyManager" + n++ + ".txt";        // get a filename (MoneyManager0/1/2..)
            try (Scanner readFile = new Scanner(new File(fname))) {
                accounts.add(new BankAccount(readFile.nextLine(), 0.0));  // add new bank account
                accounts.getLast().name = readFile.nextLine();                       // set name
                while (readFile.hasNextLine()) {
                    String line = readFile.nextLine();                      // read a transaction
                    line = line.substring(1, line.length() - 1);            // strip first and last "
                    String[] values = line.split("\",\"");            // values[] = transaction items
                    accounts.getLast().transactions.add(new ArrayList<>()); // add a new transaction
                    Collections.addAll(accounts.getLast().transactions.getLast(), values);  // add items
//                    for (String value : values)
//                        accounts.getLast().transactions.getLast().add(value);
                }
            }
            catch (FileNotFoundException e) {
                System.out.println("Error reading file.");
                return; }
        }
    }

    public static void Write(String filePath, List<BankAccount> accounts) {
        int n = 0;
        for (BankAccount account : accounts) {                              // for each bank account
            String fname = filePath + "MoneyManager" + n++ + ".txt";        // get a filename (MoneyManager0/1/2..)
            try (FileWriter writeFile = new FileWriter(fname)) {
                writeFile.write(account.name);                              // save account name
                for (ArrayList<String> transaction : account.transactions) {       // for each transaction
                    String line = "\n\"";           // start with a "
                    for (String item : transaction)
                        line += item + "\",\"";     // add each item & end with ","
                    writeFile.write(line.substring(0, (line.length() - 2)));    // chop final ,"
                }
            } catch (IOException e) {
                System.out.println("Could not save " + fname); }
        }
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
        String amount = String.format("%.2f", Input.InputDouble("Amount: "));
        String category = Input.InputStr("Category: ", 20);
        String notes = Input.InputStr("notes: ", 20);
        transactions.add(new ArrayList<>());
        Collections.addAll(transactions.getLast(), payee, date, amount, category, notes, "unused");
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
        File mac = new File("/users/shared");
        String filepath = mac.exists() ? "/users/shared/" : "/users/public/";
        FileOp.Read(filepath, accounts);

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
        FileOp.Write(filepath, accounts);
    }
}
