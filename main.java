

import java.util.*;

// Animal class
class Animal {
    int id;
    String name;
    String species;
    int health;
    int hunger;
    int zoneId;

    public Animal(int id, String name, String species, int health, int hunger, int zoneId) {
        this.id = id;
        this.name = name;
        this.species = species;
        this.health = health;
        this.hunger = hunger;
        this.zoneId = zoneId;
    }

    public int getPriority() {
        return (100 - health) + hunger;
    }

    public String toString() {
        return "Animal ID: " + id + ", " + name + ", Species: " + species + ", Health: " + health + ", Hunger: " + hunger;
    }
}

// Zone class
class Zone {
    int id;
    String name;
    LinkedList<Animal> animals;

    public Zone(int id, String name) {
        this.id = id;
        this.name = name;
        this.animals = new LinkedList<Animal>();
    }
}

// Feeding Unit class
class FeedingUnit implements Comparable<FeedingUnit> {
    String name;
    int availability;
    int popularity;

    public FeedingUnit(String name, int availability, int popularity) {
        this.name = name;
        this.availability = availability;
        this.popularity = popularity;
    }

    public int compareTo(FeedingUnit o) {
        if (this.availability != o.availability) {
            return o.availability - this.availability;
        } else {
            return o.popularity - this.popularity;
        }
    }
}

// Undo Action class
class Action {
    String type;
    Animal animal;

    public Action(String type, Animal animal) {
        this.type = type;
        this.animal = animal;
    }
}

public class main {
    static Scanner sc = new Scanner(System.in);

    static Zone[] zones = new Zone[3];
    static {
        zones[0] = new Zone(0, "Savanna");
        zones[1] = new Zone(1, "Wetlands");
        zones[2] = new Zone(2, "Rainforest");
    }

    static Map<Integer, Animal> animalMap = new HashMap<Integer, Animal>();
    static PriorityQueue<Animal> feedingQueue = new PriorityQueue<Animal>(new Comparator<Animal>() {
        public int compare(Animal a, Animal b) {
            return b.getPriority() - a.getPriority();
        }
    });
    static Stack<Action> undoStack = new Stack<Action>();

    @SuppressWarnings("unchecked")
    static ArrayList<Integer>[] graph = new ArrayList[3];

    static PriorityQueue<FeedingUnit> feedingUnits = new PriorityQueue<FeedingUnit>();

    static {
        for (int i = 0; i < 3; i++) {
            graph[i] = new ArrayList<Integer>();
        }
        graph[0].add(1); // Savanna -> Wetlands
        graph[1].add(2); // Wetlands -> Rainforest
        graph[0].add(2); // Savanna -> Rainforest

        feedingUnits.add(new FeedingUnit("Central Feeding Hub", 5, 90));
        feedingUnits.add(new FeedingUnit("North Feeding Station", 3, 70));
        feedingUnits.add(new FeedingUnit("South Feeding Station", 4, 80));
    }

    public static void main(String[] args) {
        while (true) {
            printMenu();
            int choice = sc.nextInt();
            sc.nextLine();

            // Clear console after choice
            clearConsole();

            switch (choice) {
                case 1: addAnimal(); break;
                case 2: requestFeeding(); break;
                case 3: serveFood(); break;
                case 4: checkPath(); break;
                case 5: undoAction(); break;
                case 6: showRegistry(); break;
                case 7: smartRecommendation(); break;
                case 8:
                    System.out.println("Exiting system...");
                    System.out.println("Thank you for using Smart Wildlife Sanctuary System.");
                    return;
                default: System.out.println("[ERROR] Invalid choice!"); break;
            }

            System.out.println("\nPress Enter to continue...");
            sc.nextLine(); // wait for user
            clearConsole(); // clear before showing menu again
        }
    }

    static void printMenu() {
        System.out.println("==============================");
        System.out.println("SMART WILDLIFE SANCTUARY SYSTEM");
        System.out.println("==============================");
        System.out.println("1. Add Animal");
        System.out.println("2. Request Feeding");
        System.out.println("3. Serve Food (Priority)");
        System.out.println("4. Check Path");
        System.out.println("5. Undo Last Action");
        System.out.println("6. Show Registry");
        System.out.println("7. Recommend Feeding Unit");
        System.out.println("8. Exit");
        System.out.print("Enter Choice: ");
    }

    static void clearConsole() {
        // ANSI escape code to clear screen
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    static void addAnimal() {
        System.out.println("Add Animal:");
        System.out.print("Enter ID: "); int id = sc.nextInt(); sc.nextLine();
        System.out.print("Enter Name: "); String name = sc.nextLine();
        System.out.print("Enter Species: "); String species = sc.nextLine();
        System.out.print("Health (0-100): "); int health = sc.nextInt();
        System.out.print("Hunger (0-100): "); int hunger = sc.nextInt();
        System.out.print("Zone ID (0-2): "); int zoneId = sc.nextInt(); sc.nextLine();

        Animal animal = new Animal(id, name, species, health, hunger, zoneId);
        zones[zoneId].animals.add(animal);
        animalMap.put(id, animal);
        undoStack.push(new Action("ADD", animal));

        System.out.println("[SUCCESS] Animal registered in Zone " + zones[zoneId].name);
    }

    static void requestFeeding() {
        System.out.println("Request Feeding:");
        System.out.print("Enter Animal ID: "); int id = sc.nextInt(); sc.nextLine();
        Animal a = animalMap.get(id);
        if (a != null) {
            feedingQueue.add(a);
            undoStack.push(new Action("FEED_REQUEST", a));
            System.out.println("[INFO] Feeding request added to priority queue");
        } else {
            System.out.println("[ERROR] Animal not found!");
        }
    }

    static void serveFood() {
        System.out.println("Serve Food (Priority Queue):");
        if (feedingQueue.isEmpty()) {
            System.out.println("[INFO] No feeding requests pending");
            return;
        }
        Animal a = feedingQueue.poll();
        System.out.println("[EMERGENCY FEEDING]");
        System.out.println(a);
        a.hunger = 0;
        System.out.println("[STATUS] Critical animal fed first");
        undoStack.push(new Action("FEED_SERVE", a));
    }

    static void checkPath() {
        System.out.println("Check Path (Graph DFS):");
        System.out.print("From Zone: "); int from = sc.nextInt();
        System.out.print("To Zone: "); int to = sc.nextInt(); sc.nextLine();
        boolean[] visited = new boolean[3];
        if (dfs(from, to, visited)) {
            System.out.println("[PATH CHECK] Path EXISTS between " + zones[from].name + " and " + zones[to].name);
        } else {
            System.out.println("[PATH CHECK] NO path found");
        }
    }

    static boolean dfs(int current, int target, boolean[] visited) {
        if (current == target) return true;
        visited[current] = true;
        for (int neighbor : graph[current]) {
            if (!visited[neighbor] && dfs(neighbor, target, visited)) return true;
        }
        return false;
    }

    static void undoAction() {
        System.out.println("Undo Last Action (Stack):");
        if (undoStack.isEmpty()) {
            System.out.println("[INFO] No actions to undo");
            return;
        }
        Action last = undoStack.pop();
        if (last.type.equals("ADD")) {
            zones[last.animal.zoneId].animals.remove(last.animal);
            animalMap.remove(last.animal.id);
        } else if (last.type.equals("FEED_REQUEST")) {
            feedingQueue.remove(last.animal);
        } else if (last.type.equals("FEED_SERVE")) {
            last.animal.hunger = 50;
        }
        System.out.println("[UNDO] Last action reverted successfully");
    }

    static void showRegistry() {
        System.out.println("Show Registry (Linked List):");
        System.out.println("--- ZONE REGISTRY ---");
        for (Zone z : zones) {
            System.out.println("Zone " + z.id + ": " + z.name);
            if (z.animals.isEmpty()) System.out.println("(No animals present)");
            else for (Animal a : z.animals) System.out.println(a);
            System.out.println();
        }
    }

    static void smartRecommendation() {
        System.out.println("Smart Query (Heap-based Recommendation):");
        if (feedingQueue.isEmpty()) {
            System.out.println("[INFO] No feeding requests to analyze");
            return;
        }
        Animal topAnimal = feedingQueue.peek();
        FeedingUnit bestUnit = feedingUnits.peek();

        System.out.println("[SMART FEEDING QUERY]");
        System.out.println("Analyzing feeding units for Animal ID: " + topAnimal.id + ", " + topAnimal.name);
        System.out.println("Priority: " + topAnimal.getPriority());
        System.out.println("Availability & Popularity Check");

        System.out.println("[RECOMMENDATION]");
        System.out.println("Best Feeding Unit: " + bestUnit.name);
        System.out.println("Reason: High availability + Priority match");
    }
}
