package thriftstore;

import java.util.ArrayList;
import java.util.List;

public class Store {
    public static final String[] SECTION_NAMES = {"Electronics", "Clothing", "Furniture", "Toys", "Sporting Goods", "Books"};

    public List<Section> sections = new ArrayList<>(); // list of sections

    public Store(){
        for (String section : SECTION_NAMES){
            Section sect = new Section(section, 5); // initialize every section to 5 items
            this.sections.add(sect); 
        }
    }

    public synchronized Section getSection(String sect_name){
        for (Section sect : this.sections){
            if (sect.section_name.equals(sect_name)){
                return sect;
            }
        }
        return null; 
    }
}