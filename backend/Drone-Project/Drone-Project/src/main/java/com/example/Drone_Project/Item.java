// ItemForDelivery Class - this class represents an item that is available for delivery, including its ID, name, and the pickup station. It also contains a static inner class to manage the current items available for delivery.

package com.example.Drone_Project;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;



public class Item implements Comparable<Item> {
    private int itemIdInt;
    private String itemId;
    private String itemName;
    private PickupStation pickUpStation;
    private Node targetLocation;
    public boolean priority;
    public String[] items = {"Large Package", "Small Package", "Medium Package", "Fragile Item", "Heavy Item"};
    public long addedTo;
    public static int itemCount = 0;
    public Item(String itemId, String itemName, PickupStation pickUpStation, boolean priority, Node targetLocation) {

        this.itemIdInt = itemCount;
        this.itemId = "Priority" + itemId;
        this.itemName = "Priority" + itemName;
        this.pickUpStation = pickUpStation;
        this.targetLocation = targetLocation;
        this.priority = priority;
        this.addedTo = System.currentTimeMillis();
        itemCount++;

    }

    public Item(String itemId, String itemName, PickupStation pickUpStation, Node targetLocation) {
        this.itemIdInt = itemCount;

        this.itemId = itemId ;
        this.itemName = itemName ;
        this.pickUpStation = pickUpStation;
        this.targetLocation = targetLocation;
        this.priority = false; 
        this.addedTo = System.currentTimeMillis();
        itemCount++;



    }

     public Item(String itemId, PickupStation pickUpStation, Node targetLocation) {
        this.itemIdInt = itemCount;

        this.itemId = itemId ;
        this.itemName = "unNamedItem";
        this.pickUpStation = pickUpStation;
        this.targetLocation = targetLocation;
        this.priority = false; 
        this.addedTo = System.currentTimeMillis();
        itemCount++;
    
    }

    public String getItemId() {
        return itemId;
    }


    public String getItemName() {
        return itemName;

    }


    public Node getTargetLocation() {
        return targetLocation;
    }


    public void setTargetLocation(Node targetLocation) {
        this.targetLocation = targetLocation;
    }

    @Override
    public int compareTo(Item other) {
        if (this.priority && !other.priority) return -1;
        if (!this.priority && other.priority) return 1;
        return Integer.compare(this.itemIdInt, other.itemIdInt);
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
    public void setItemName(String itemName) {
        this.itemName = itemName;
    }
    public int getItemIdInt() {
        return itemIdInt;
    }
    public void setItemIdInt(int itemIdInt) {
        this.itemIdInt = itemIdInt;
    }
    public PickupStation getPickUpStation() {
        return pickUpStation;
    }
    public void setPickUpStation(PickupStation pickUpStation) {
        this.pickUpStation = pickUpStation;
    }
    @Override
    public String toString() {
        return "ItemForDelivery{" +
                "itemId='" + itemId + '\'' +
                // ", itemName='" + itemName + '\'' +
                // ", pickUpStation=" + pickUpStation +
                ", targetLocation=" + targetLocation +
                '}';
    }

    static class CurrentItemsAvailable {
        private static final PriorityBlockingQueue<Item> itemsForDelivery = new PriorityBlockingQueue<>();

        private static final ConcurrentHashMap<Integer, Item> itemsMap = new ConcurrentHashMap<>();


        public static PriorityBlockingQueue<Item> getItemsForDelivery() {
            return itemsForDelivery;
        }

        public static boolean isEmpty() {
            return itemsForDelivery.isEmpty();
        }

        public static void addItem(Item item) {
            itemsForDelivery.add(item);
            itemsMap.put(item.itemIdInt, item);
        }

        public static void removeItem(Item item) {
           itemsForDelivery.remove(item);
           itemsMap.remove(item.itemIdInt);     
        }

        public static ConcurrentHashMap<Integer, Item> getItemsMap(){
            return itemsMap;
        }
    }   
}
