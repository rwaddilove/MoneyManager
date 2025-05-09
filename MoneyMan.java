// Money Manager is a text-based command line money manager by
// Roland Waddilove (github.com/rwaddilove/) as a learning exercise.
// I'm learning Java. This is just to practise what I have learnt
// so far. There's still a lot I don't know, so it's not perfect.
// Public Domain. Use any way you like - at your own risk!

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

    public static String InputDate() {
        String prompt = "Date (" + LocalDate.now().toString() + "): ";
        String inp = InputStr(prompt,15);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd");
        try {
            LocalDate date = LocalDate.parse(inp, formatter); }
        catch (DateTimeParseException dtpe) {
//            System.out.println("Date not recognised, using today.");
            inp = LocalDate.now().toString(); }
        return inp; }
}


class FileOp {

    public static void Read(String filePath, List<BankAccount> accounts) {
        // create bank accounts, add filenames to accountFiles
        int n = 0;
        while (true) {
            String fname = filePath + "MoneyManager" + n++ + ".txt";        // get a filename (MoneyManager0/1/2..)
            try (Scanner readFile = new Scanner(new File(fname))) {
                String name = readFile.nextLine();
                double startBalance = Double.parseDouble(readFile.nextLine());
                accounts.add(new BankAccount(name, startBalance));  // add new bank account
                while (readFile.hasNextLine()) {
                    String line = readFile.nextLine();                      // read a transaction
                    line = line.substring(1, line.length() - 1);            // strip first and last "
                    String[] values = line.split("\",\"");            // values[] = transaction items
                    accounts.getLast().transactions.add(new ArrayList<>()); // add a new transaction
                    Collections.addAll(accounts.getLast().transactions.getLast(), values);  // add items
                }
            }
            catch (FileNotFoundException e) {
//                System.out.println("No accounts file found.");
                return; }
        }
    }

    public static void Write(String filePath, List<BankAccount> accounts) {
        // files named MoneyManager0.txt, ..1.txt, ..2.txt, etc.
        int n = 0;
        for (BankAccount account : accounts) {                              // for each bank account
            String fname = filePath + "MoneyManager" + n++ + ".txt";        // get next filename
            try (FileWriter writeFile = new FileWriter(fname)) {
                writeFile.write(account.name + "\n" + account.startBalance + "\n");
                for (ArrayList<String> transaction : account.transactions) {       // for each transaction
                    String line = "\"";                 // start with a "
                    for (String item : transaction)
                        line += item + "\",\"";         // add each item & end with ","
                    writeFile.write(line.substring(0, (line.length() - 2)) + "\n");     // chop final ,"
                }
            } catch (IOException e) {
                System.out.println("Could not save " + fname); }
        }
        // must delete old files if when account is deleted
        for (int i = n; i < (n+10); ++i) {
            File file = new File(filePath + "MoneyManager" + i + ".txt");
            file.delete(); }
    }
}


class BankAccount {
    String name;
    double balance;
    double startBalance;
    ArrayList<ArrayList<String>> transactions = new ArrayList<>();

    public BankAccount(String name, double money) {
        this.name = name;
        this.startBalance = money;
        this.balance = money;
    }

    public double GetBalance(String label) {
        double total = 0.0;
        if (!transactions.isEmpty()) {
            for (ArrayList<String> item : transactions) {
                if (label.equals(item.get(3)) || label.isEmpty())
                    total += Double.parseDouble(item.get(2));
            }
        }
        if (label.isEmpty()) {      // don't do this if getting category transactions
            total += startBalance;
            balance = total; }      // update balance
        return total;
    }

    public void ShowAccount(List<BankAccount> accounts) {
        String filterTransactions = "";
        int showTrans = 10;                  // how many transactions to show
        String cmd = "";
        while (!cmd.startsWith("c")) {
            ShowTransactions(filterTransactions, showTrans);        // show most recent transactions
            cmd = Input.InputStr("\nNum/Add/Edit/Filter/More/Delete/Close: ", 5).toLowerCase();
            if (cmd.startsWith("a")) AddTransaction();
            if (cmd.startsWith("e")) EditTransaction();
            if (cmd.startsWith("f")) filterTransactions = Input.InputStr("Filter by category: ", 12);
            if (cmd.startsWith("m")) showTrans += 10;
            if (cmd.startsWith("d")) System.out.println(DeleteTransaction(accounts));
            if (Input.isInteger(cmd)) {                 // if a number is entered
                int taction = Integer.parseInt(cmd);    // show transaction details
                if (taction > -1 && taction < transactions.size()) ShowTransactionDetails(taction); }
        }
    }

    public void ShowTransactionDetails(int taction) {
        System.out.printf("\nTRANSACTION %03d DETAILS\n", taction);
        System.out.println("Payee: " + transactions.get(taction).get(0));
        System.out.println("Date: " + transactions.get(taction).get(1));
        System.out.println("Amount: £" + transactions.get(taction).get(2));
        System.out.println("Category: " + transactions.get(taction).get(3));
        System.out.println("Notes: " + transactions.get(taction).get(4));
        Input.InputStr("\nPress Enter...", 5);
    }

    public String DeleteTransaction(List<BankAccount> accounts) {
        if (transactions.isEmpty()) return "No transactions to delete!";
        int taction = Input.InputInt("Transaction to delete? ");
        if (taction < 0 || taction >= transactions.size()) return "Can't find that transaction!";
        String id = transactions.get(taction).get(5);   // is this a money transfer?
        transactions.remove(taction);
        if (id.equals("unused")) return "Transaction deleted";          // not a money transfer
        // find & delete the corresponding transfer in other accounts
        for (BankAccount account : accounts) {                          // for each bank account
            if (account.transactions.isEmpty()) continue;               // skip if no transactions
            for (int j = 0; j < account.transactions.size(); ++j) {     // for each transaction
                if (account.transactions.get(j).get(5).equals(id)) {    // is it the right one?
                    account.transactions.remove(j);                     // remove it
                    break; }                                            // list changed, stop!
            }
        }
        return "Transaction deleted";
    }

    public void ShowTransactions(String label, int showTrans) {
        System.out.print("\nLAST "+ showTrans + " TRANSACTIONS FOR: " + name);
        System.out.println("\n--- Payee ------------ Date ------- £xxxx.xx - Category");
        if (transactions.isEmpty()) return;
        showTrans = (showTrans > transactions.size()) ? 0 : transactions.size()-showTrans; // transactions to show
        for (int i = showTrans; i < transactions.size(); ++i) {
            if (label.isEmpty() || label.equals(transactions.get(i).get(3))) {
                String payee = transactions.get(i).get(0);
                if (payee.length() > 16) payee = payee.substring(0, 16) + "..";
                System.out.printf("%03d %-18s %-10s %10s   %s\n", i, payee, transactions.get(i).get(1), transactions.get(i).get(2), transactions.get(i).get(3));
            }
        }
        System.out.printf("                            Total %10.2f", GetBalance(label));
    }

    public void AddTransaction() {
        // payee, date, amount, category, notes, id
        System.out.println("\nADD TRANSACTION:");
        String payee = Input.InputStr("Payee: ", 30);
        if (payee.isBlank()) return;
        String amount = String.format("%.2f", Input.InputDouble("Amount (eg. -7.50): "));
        if (amount.equals("0.00")) return;
        String date = Input.InputDate();
        String category = Input.InputStr("Category: ", 12);
        String notes = Input.InputStr("notes: ", 250);
        transactions.add(new ArrayList<>());
        Collections.addAll(transactions.getLast(), payee, date, amount, category, notes, "unused");
        SortTransactions();
    }

    public void MoneyTransfer(String payee, String date, String amount, String category, String notes, String id) {
        transactions.add(new ArrayList<>());
        Collections.addAll(transactions.getLast(), payee, date, amount, category, notes, id);
        SortTransactions();
    }

    public void EditTransaction() {
        if (transactions.isEmpty()) return;
        int tn = Input.InputInt("Transaction to edit: ");
        if (tn < 0 || tn >= transactions.size()) {
            System.out.println("Can't find that transaction!");
            return; }
        System.out.println("EDIT TRANSACTION:");
        System.out.println("0 Payee: " + transactions.get(tn).get(0));
        System.out.println("1 Date: " + transactions.get(tn).get(1));
        System.out.println("2 Amount: " + transactions.get(tn).get(2));
        System.out.println("3 Category: " + transactions.get(tn).get(3));
        System.out.println("4 Notes: " + transactions.get(tn).get(4));
        int item = Input.InputInt("Item to edit (0-4): ");
        if (item < 0 || item > 4) {
            System.out.println("Can't find that transaction!");
            return; }
        String inp = "";
        if (item == 0) {
            inp = Input.InputStr("Payee: ", 30);
            if (inp.isBlank()) return; }
        if (item == 2) {
            inp = String.format("%.2f", Input.InputDouble("Amount (eg. -7.45): "));
            if (inp.equals("0.00")) return; }
        if (item == 1) inp = Input.InputDate();
        if (item == 3) inp = Input.InputStr("Category: ", 12);
        if (item == 4) inp = Input.InputStr("notes: ", 250);
        transactions.get(tn).set(item, inp);
        SortTransactions();
    }

    public void SortTransactions() {
        if (transactions.size() < 2) return;    // can't sort
        if (transactions.size() == 2) {         // simple sort for 2 transactions
            if (transactions.get(0).get(1).compareTo(transactions.get(1).get(1)) > 0) {
                Collections.swap(transactions, 0, 1);
                return; } }
        // 3 or more transactions can be sorted
        for (int i = 0; i < transactions.size() - 1; ++i)     // 3 or more transactions
            for (int j = transactions.size() - 2; j >= i; --j)
                if (transactions.get(j).get(1).compareToIgnoreCase(transactions.get(j + 1).get(1)) > 0)
                    Collections.swap(transactions, j, j + 1);
    }
}


class General {     // general functions not specific to any account
    public static void Tips() {
        System.out.println("\nTIPS:");
        System.out.println("First time use - add an account!");
        System.out.println("No date (hit Enter) or bad date = today.");
        System.out.println("Press Enter for name or amount to abort.");
        System.out.println("An expense or bill is minus, eg. -7.50");
        System.out.println("Enter a number to select an account or transaction.");
    }

    public static void AddAccount(List<BankAccount> accounts) {
        String name = Input.InputStr("ADD NEW ACCOUNT:\nEnter name: ", 20);
        if (name.isBlank()) {
            System.out.println("Account must have a name!");
            return; }
        double openingBalance = Input.InputDouble("Enter opening balance: ");
        accounts.add(new BankAccount(name, openingBalance));
    }

    public static void ListBankAccounts(List<BankAccount> accounts) {
        System.out.println("\nYOUR ACCOUNTS:");
        if (accounts.isEmpty()) {
            System.out.println("None found: Add one!");
            return; }
        for (int i = 0; i < accounts.size(); ++i) {
            String accountName = accounts.get(i).name + "                    ";
            System.out.printf("%-2d %-16s  Balance: £%10s\n", i, accountName.substring(0, 16), accounts.get(i).GetBalance(""));
        }
    }

    public static void DeleteAccount(List<BankAccount> accounts) {
        if (accounts.isEmpty()) {
            System.out.println("No account to delete!");
            return; }
        int acc = Input.InputInt("Enter account num to delete: ");
        if (acc < 0 || acc >= accounts.size()) {
            System.out.println("Couldn't find that account!");
            return; }
        accounts.remove(acc);
    }

    public static String MoveMoney(List<BankAccount> accounts) {
        if (accounts.size() < 2) return "You need 2 or more accounts!";
        System.out.println("MOVE MONEY FROM/TO ACCOUNT");
        int from = Input.InputInt("Move from: ");
        int to = Input.InputInt("Move to: ");
        if (from < 0 || from >= accounts.size() || to < 0 || to >= accounts.size() || from == to) return "Can't do that!";

        String payeeFrom = "Transfer from " + accounts.get(from).name;
        String date = Input.InputDate();
        String amount = String.format("%.2f", Input.InputDouble("Amount: "));
        String category = "transfer";
        String notes = Input.InputStr("notes: ", 250);
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");
        String id = new Date().toString();
        accounts.get(to).MoneyTransfer(payeeFrom, date, amount, category, notes, id);

        String payeeTo = "Transfer to " + accounts.get(to).name;
        amount = "-" + amount;
        accounts.get(from).MoneyTransfer(payeeTo, date, amount, category, notes, id);
        return "Transferred £" + amount + " from " + accounts.get(from).name + " to " + accounts.get(to).name;
    }
}


public class MoneyMan {
    public static void main(String[] args) {
        List<BankAccount> accounts = new ArrayList<>();
        String filepath = System.getProperty("user.home") + System.getProperty("file.separator");
        FileOp.Read(filepath, accounts);      // save location is user's home folder
        General.Tips();
        while (true) {
            // list all the accounts
            System.out.println("\n---------- RAW MONEY MANAGER ------------");
            General.ListBankAccounts(accounts);
            String cmd = Input.InputStr("\nAccount: Num/Add/Move/Delete/Quit: ", 5).toLowerCase();
            if (cmd.startsWith("q")) break;
            if (cmd.startsWith("a")) General.AddAccount(accounts);
            if (cmd.startsWith("d")) General.DeleteAccount(accounts);
            if (cmd.startsWith("m")) System.out.println(General.MoveMoney(accounts));
            // if account number entered - show that account
            if (Input.isInteger(cmd)) {
                int account = Integer.parseInt(cmd);
                if (account > -1 && account < accounts.size())
                    accounts.get(account).ShowAccount(accounts); }
        }
        FileOp.Write(filepath, accounts);
    }
}
