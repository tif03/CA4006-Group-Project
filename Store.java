// class defining thrift store
public class Store {
    private List<Section> sections = new ArrayList<>(); // list of sections

    Store(){
        for (String section : SECTION_NAMES){
            Section sect = new Section(section, 5); // initialize every section to 5 items
            this.sections.add(sect); 
        }
    }

    public Section getSection(String sect_name){
        for (Section sect : this.sections){
            if (sect.section_name.equals(sect_name)){
                return sect;
            }
        }
        //TODO we have to change this o.o
        return new Section("kill me", 0); 
    }
}