package com;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {
    private static final String NOTES_DIRECTORY = "notes/";
    private static final Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        createNotesDirectory();
        
        System.out.println("📝 Welcome to Console Notes App 📝");
        System.out.println("==================================");
        
        boolean running = true;
        while (running) {
            displayMenu();
            int choice = getMenuChoice();
            
            switch (choice) {
                case 1 -> createNote();
                case 2 -> viewNotes();
                case 3 -> searchNotes();
                case 4 -> deleteNote();
                case 5 -> {
                    System.out.println("\nGoodbye! 👋");
                    running = false;
                }
                default -> System.out.println("❌ Invalid choice. Please try again.");
            }
        }
        scanner.close();
    }

    private static void displayMenu() {
        System.out.println("\n═══════════════════════════════");
        System.out.println("          NOTES MENU           ");
        System.out.println("═══════════════════════════════");
        System.out.println("1. 📝 Create New Note");
        System.out.println("2. 📄 View All Notes");
        System.out.println("3. 🔍 Search Notes");
        System.out.println("4. 🗑️  Delete Note");
        System.out.println("5. ❌ Exit");
        System.out.println("═══════════════════════════════");
        System.out.print("Enter your choice (1-5): ");
    }

    private static int getMenuChoice() {
        try {
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static void createNotesDirectory() {
        try {
            Files.createDirectories(Paths.get(NOTES_DIRECTORY));
            System.out.println("✅ Notes directory is ready!");
        } catch (IOException e) {
            System.err.println("❌ Error creating notes directory: " + e.getMessage());
        }
    }

    private static void createNote() {
        System.out.println("\n═══════════════════════════════");
        System.out.println("        CREATE NEW NOTE        ");
        System.out.println("═══════════════════════════════");
        
        System.out.print("Enter note title: ");
        String title = scanner.nextLine().trim();
        
        if (title.isEmpty()) {
            System.out.println("❌ Title cannot be empty!");
            return;
        }

        System.out.println("Enter note content (type 'END' on a new line to finish):");
        System.out.println("───────────────────────────────────");
        
        StringBuilder content = new StringBuilder();
        String line;
        
        while (true) {
            line = scanner.nextLine();
            if (line.equals("END")) {
                break;
            }
            content.append(line).append("\n");
        }

        String filename = title.replaceAll("[^a-zA-Z0-9-_ ]", "_") + ".txt";
        String timestamp = LocalDateTime.now().format(formatter);
        
        try {
            Path notePath = Paths.get(NOTES_DIRECTORY, filename);
            
            if (Files.exists(notePath)) {
                System.out.print("Note with this name exists. Overwrite? (y/n): ");
                String response = scanner.nextLine().trim().toLowerCase();
                if (!response.equals("y") && !response.equals("yes")) {
                    System.out.println("❌ Note creation cancelled.");
                    return;
                }
            }
            
            String fullContent = "Title: " + title + "\n"
                    + "Created: " + timestamp + "\n"
                    + "Modified: " + timestamp + "\n"
                    + "═══════════════════════════════\n"
                    + content.toString().trim() + "\n";
            
            Files.writeString(notePath, fullContent);
            System.out.println("✅ Note saved successfully: " + filename);
            System.out.println("📁 Location: " + notePath.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("❌ Error saving note: " + e.getMessage());
        }
    }

    private static void viewNotes() {
        System.out.println("\n═══════════════════════════════");
        System.out.println("         YOUR NOTES           ");
        System.out.println("═══════════════════════════════");
        
        try {
            List<Path> notes = Files.list(Paths.get(NOTES_DIRECTORY))
                    .filter(path -> path.toString().endsWith(".txt"))
                    .sorted()
                    .toList();
            
            if (notes.isEmpty()) {
                System.out.println("📭 No notes found. Create your first note!");
                return;
            }
            
            System.out.println("Found " + notes.size() + " note(s):");
            System.out.println("───────────────────────────────────");
            
            for (int i = 0; i < notes.size(); i++) {
                Path note = notes.get(i);
                try {
                    String firstLine = Files.lines(note)
                            .findFirst()
                            .orElse("Untitled")
                            .replace("Title: ", "");
                    System.out.printf("%d. %-30s - %s%n", 
                        (i + 1), 
                        note.getFileName().toString().replace(".txt", ""),
                        firstLine.substring(0, Math.min(40, firstLine.length())) + 
                        (firstLine.length() > 40 ? "..." : ""));
                } catch (IOException e) {
                    System.out.println((i + 1) + ". " + note.getFileName() + " (Error reading)");
                }
            }
            
            System.out.print("\nEnter note number to view (0 to go back): ");
            int choice = getMenuChoice();
            
            if (choice > 0 && choice <= notes.size()) {
                viewNoteDetail(notes.get(choice - 1));
            }
        } catch (IOException e) {
            System.err.println("❌ Error reading notes: " + e.getMessage());
        }
    }

    private static void viewNoteDetail(Path notePath) {
        try {
            String content = Files.readString(notePath);
            System.out.println("\n" + "═".repeat(50));
            System.out.println(content);
            System.out.println("═".repeat(50));
            
            System.out.println("\nOptions:");
            System.out.println("1. ✏️  Edit this note");
            System.out.println("2. 🗑️  Delete this note");
            System.out.println("3. ↩️  Back to list");
            System.out.print("Choose (1-3): ");
            
            int choice = getMenuChoice();
            switch (choice) {
                case 1 -> editNote(notePath);
                case 2 -> deleteSingleNote(notePath);
            }
        } catch (IOException e) {
            System.err.println("❌ Error reading note: " + e.getMessage());
        }
    }

    private static void editNote(Path notePath) {
        try {
            String oldContent = Files.readString(notePath);
            String[] lines = oldContent.split("\n", 4);
            
            if (lines.length < 4) {
                System.out.println("❌ Invalid note format.");
                return;
            }
            
            System.out.println("\nCurrent content:");
            System.out.println("───────────────────────────────────");
            System.out.println(lines[3]); // Show only the content part
            
            System.out.println("\nEnter new content (type 'END' on a new line to finish):");
            System.out.println("───────────────────────────────────");
            
            StringBuilder newContent = new StringBuilder();
            scanner.nextLine(); // Clear buffer
            
            String line;
            while (true) {
                line = scanner.nextLine();
                if (line.equals("END")) {
                    break;
                }
                newContent.append(line).append("\n");
            }
            
            String timestamp = LocalDateTime.now().format(formatter);
            String updatedContent = lines[0] + "\n"
                    + lines[1] + "\n"
                    + "Modified: " + timestamp + "\n"
                    + "═══════════════════════════════\n"
                    + newContent.toString().trim() + "\n";
            
            Files.writeString(notePath, updatedContent);
            System.out.println("✅ Note updated successfully!");
            
        } catch (IOException e) {
            System.err.println("❌ Error editing note: " + e.getMessage());
        }
    }

    private static void searchNotes() {
        System.out.println("\n═══════════════════════════════");
        System.out.println("         SEARCH NOTES          ");
        System.out.println("═══════════════════════════════");
        System.out.print("Enter search term: ");
        String searchTerm = scanner.nextLine().toLowerCase();
        
        if (searchTerm.trim().isEmpty()) {
            System.out.println("❌ Search term cannot be empty!");
            return;
        }
        
        try {
            List<Path> notes = Files.list(Paths.get(NOTES_DIRECTORY))
                    .filter(path -> path.toString().endsWith(".txt"))
                    .filter(path -> {
                        try {
                            String content = Files.readString(path).toLowerCase();
                            return content.contains(searchTerm);
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .sorted()
                    .toList();
            
            if (notes.isEmpty()) {
                System.out.println("🔍 No notes found containing: \"" + searchTerm + "\"");
                return;
            }
            
            System.out.println("\nFound " + notes.size() + " note(s) containing \"" + searchTerm + "\":");
            System.out.println("───────────────────────────────────");
            
            for (int i = 0; i < notes.size(); i++) {
                System.out.println((i + 1) + ". " + notes.get(i).getFileName());
            }
            
            System.out.print("\nEnter note number to view (0 to go back): ");
            int choice = getMenuChoice();
            
            if (choice > 0 && choice <= notes.size()) {
                viewNoteDetail(notes.get(choice - 1));
            }
        } catch (IOException e) {
            System.err.println("❌ Error searching notes: " + e.getMessage());
        }
    }

    private static void deleteNote() {
        System.out.println("\n═══════════════════════════════");
        System.out.println("         DELETE NOTE           ");
        System.out.println("═══════════════════════════════");
        
        try {
            List<Path> notes = Files.list(Paths.get(NOTES_DIRECTORY))
                    .filter(path -> path.toString().endsWith(".txt"))
                    .sorted()
                    .toList();
            
            if (notes.isEmpty()) {
                System.out.println("📭 No notes to delete.");
                return;
            }
            
            System.out.println("Select note to delete:");
            System.out.println("───────────────────────────────────");
            
            for (int i = 0; i < notes.size(); i++) {
                System.out.println((i + 1) + ". " + notes.get(i).getFileName());
            }
            
            System.out.print("\nEnter note number to delete (0 to cancel): ");
            int choice = getMenuChoice();
            
            if (choice > 0 && choice <= notes.size()) {
                deleteSingleNote(notes.get(choice - 1));
            }
        } catch (IOException e) {
            System.err.println("❌ Error listing notes: " + e.getMessage());
        }
    }

    private static void deleteSingleNote(Path notePath) {
        try {
            System.out.print("\n⚠️  Are you sure you want to delete '" + 
                notePath.getFileName() + "'? (y/n): ");
            String confirm = scanner.nextLine().trim().toLowerCase();
            
            if (confirm.equals("y") || confirm.equals("yes")) {
                Files.delete(notePath);
                System.out.println("✅ Note deleted successfully!");
            } else {
                System.out.println("❌ Deletion cancelled.");
            }
        } catch (IOException e) {
            System.err.println("❌ Error deleting note: " + e.getMessage());
        }
    }
}