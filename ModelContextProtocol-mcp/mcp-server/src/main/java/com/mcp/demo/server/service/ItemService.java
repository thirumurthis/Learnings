package com.mcp.demo.server.service;

import com.mcp.demo.server.data.Item;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ItemService {

    public static final Logger log = LoggerFactory.getLogger(ItemService.class);

    public List<Item> items = new ArrayList<>();

    @Tool(name="t_get_all_items",description = "This method will get all the items stored in-memory list in this application")
    public List<Item> getItems (){
        return items;
    }

    @Tool(name="t_get_item_by_name",description = "This method will fetch one item based on the input name from the in-memory item lists, the name shouldn't be null")
    public Item getItem(String name) throws IllegalArgumentException {
        if(name == null || name.isEmpty()){
            log.error("Name can't be empty");
            throw new IllegalArgumentException("Name can't be empty for this request - t_get_item_by_name service");
        }
        return items.stream()
                .filter(car ->  car.name().equals(name))
                .findFirst()
                .orElse(null);
    }

    @Tool(name="t_add_item_to_list",description = "This method will add a single item to the in-memory list. " +
            "The inputItem argument in the method should be an Item object which includes name and quantity field." +
            "Before accessing this tool functionality from the client create the Item object with name and quantity" +
            "and this function will add it to the in-memory list")
    public String addItem(Item inputItem) throws IllegalArgumentException{
        if(inputItem == null || inputItem.name() == null || inputItem.name().isEmpty()){
            log.error("input Item name can't be empty");
            throw new IllegalArgumentException("Input Item name can't be empty");
        }
        //Item item = new Item(inputItem.name(),inputItem.quantity(),indexCounter.incrementAndGet());
        //inputItem.id();
        items.add(inputItem);
        return inputItem.toString();
    }

    @PostConstruct
    public void init(){
       List<Item> carList = List.of(
                new Item("Table",156),
               new Item("Chair",510),
               new Item("Cups",500),
               new Item("Bottle",43),
               new Item("Box",600)
        );
        items.addAll(carList);
    }

}
