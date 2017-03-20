import classfile.Aron;
import classfile.Print;
import java.util.*;
import java.util.List;

/**
 * convert a text file to a two dimension list
 * The first of the each list contains the key and file type are separated by colon
 */
final class ProcessList {
    private String fName;
    private Map<String, Set<String>> prefixSuffixMap = new HashMap<>();
    final Map<String, List<List<String>>> mapList = new HashMap<>();
    final Map<String, Set<String>> prefixFullKeyMap = new HashMap<>();
    Map<String, Set<List<String>>> prefixWordMap = new HashMap<>();
    Map<String, Set<String>> wordsCompletion = new HashMap<>();

    public ProcessList(String fName) {
//        String fName = "/Users/cat/myfile/github/snippets/snippet_test.m";
        this.fName = fName;
        List<List<String>> list2d = readCodeFile(this.fName);
        buildAutoCompletionKeyCodeMap(list2d);
    }
    public ProcessList(List<String> listFile) {
//        String fName = "/Users/cat/myfile/github/snippets/snippet_test.m";

        List<List<String>> list2d = new ArrayList<>();

        for(String fName : listFile) {
            List<List<String>> lists1 = readCodeFile(fName);
            list2d = Aron.mergeLists(list2d, lists1);
        }
        buildAutoCompletionKeyCodeMap(list2d);
    }

    /**
     * read the contents of file and store it in a two dimension array
     * one or more empty lines separates each "block of code" in fileName
     *
     * @param fileName is name of file
     * @return a two dimension array contains the contents of fName
     *
     * Note: each List<String> contains one "block of code"
     */
    private List<List<String>> readCodeFile(String fileName){
        List<String> list = Aron.readFileWithWhiteSpace(fileName);
        List<List<String>> lists = new ArrayList<>();

        List<String> line = new ArrayList<>();
        for(String s : list){

            if(s.trim().length() > 0){
                line.add(s);
            }else{
                if(line.size() > 0) {
                    lists.add(line);
                    line = new ArrayList<>();
                }
            }
        }
        return lists;
    }

    /**
     *
     *
     * @param lists contains the contents of file
     *
     * [dog : *.java
     * my code]
     *
     * mapList contains following
     * d ->[....]
     * do ->[...]
     * dog ->[...]
     */
    private void buildAutoCompletionKeyCodeMap(List<List<String>> lists){
        List<String> listKeys = new ArrayList<>();
        for(List<String> list : lists){
            if(list.size() > 0){

                List<String> splitKeys = Aron.split(list.get(0), ":");
                if(splitKeys.size() > 0){

                    String abbreviation = splitKeys.get(0).toLowerCase().trim();
                    Print.pbl("key=" + abbreviation);

                    if(splitKeys.size() > 2) {
                        Map<String, Set<List<String>>> oneBlockMap = prefixWordMap(splitKeys.get(2).toLowerCase().trim(), list);
                        prefixWordMap = Aron.mergeMapSet(prefixWordMap, oneBlockMap);
                    }

                    listKeys.add(abbreviation);

                    // Given a string as abbreviation , generate all prefixes as keys,
                    // use abbreviation as value to create a map: mapList. <prefix, abbreviation>
                    //
                    // Example:
                    // key = "cmd"
                    // mapList = c->[cmd], cm->[cmd], cmd->[cmd]
                    //
                    for(int i=0; i<abbreviation.length(); i++){
                        String prefix = abbreviation.substring(0, i+1);
                        Print.pbl("prefix=" + prefix);
                        List<List<String>> values = mapList.get(prefix);

                        if(values != null){
                            values.add(list);
                        }else{
                            List<List<String>> tmpLists = new ArrayList<>();
                            tmpLists.add(list);
                            mapList.put(prefix, tmpLists);
                        }
                    }
                }
            }
        }
        prefixSuffixMap = buildPrefixMap(listKeys);
        buildFullKeyMap(prefixSuffixMap);

    }

    private void buildFullKeyMap(Map<String, Set<String>> map){
        Set<String> set = new HashSet<>();
        for(Map.Entry<String, Set<String>> entry : map.entrySet()){
            for(String s : entry.getValue()) {
                set.add(entry.getKey() + s);
            }
            prefixFullKeyMap.put(entry.getKey(), set);
            set = new HashSet<>();
        }
    }

    /**
     * The method splits the "search key" to prefix and suffix, and store
     * the prefix as key and the suffix as value in HashSet
     *
     * @param list is list of string that contains all the search string
     *
     * @return a map contains key which is prefix of the "search string" and
     *          value which is suffix of "search string".
     */
    private Map<String, Set<String>> buildPrefixMap(List<String> list){
        Map<String, Set<String>> map = new HashMap<>();
        for(String str : list) {
            for (int i = 0; i < str.length() - 1; i++) {
                String prefix = str.substring(0, i + 1);
                String suffix = str.substring(i + 1, str.length());
                Set<String> set = map.get(prefix);
                if (set == null)
                    set = new HashSet<>();

                set.add(suffix);
                map.put(prefix, set);
            }
        }
        return map;
    }
    /**
     * Example:
     * jlist_list : * : vi cmd, java cool
     * my awesome code1
     *
     * str = vi cmd, java cool
     *
     * listCode contains two lines
     *
     * @param str is used to generate prefixes
     * @param listCode contains (code or string), including the first line
     * @return map contains
     *          {"j" -> listCode.sublist[1, listCode.size()]}
     *          {"ja" -> listCode.sublist[1, listCode.size()]}
     *          {"jav" -> listCode.sublist[1, listCode.size()]}
     *          {"java" -> listCode.sublist[1, listCode.size()]}
     *          {"java " -> listCode.sublist[1, listCode.size()]}
     *          ...
     */
    private Map<String, Set<List<String>>> prefixWordMap(String str, List<String> listCode){
        Map<String, Set<List<String>>> mapSet = new HashMap<>();

        if(listCode.size() > 1) {
            List<String> list = Aron.splitTrim(str, ",");
            // list = ["vi cmd", "java cool"]

            for (String words : list) {
                // words = "vi cmd"
                //
                // "v" -> "vi cmd"
                // "vi" -> "vi cmd"
                // "vi " -> "vi cmd"
                // "vi c" -> "vi cmd"
                // "vi cm" -> "vi cmd"
                // "vi cmd" -> "vi cmd"
                //
                // "c" -> "vi cmd"
                // "cm" -> "vi cmd"
                // "cmd" -> "vi cmd"
                List<String> wlist = Aron.split(words, "\\s+");
                prefixStringMap(wlist, wordsCompletion);

                // dog =>  dog->set("")
                // dog cat => dog->set("cat")
                // dog cat cow => dog -> set("cat cow")
                //             => dog cat -> set("cow")
                //             => dog cat cow -> set("")
                List<String> listWord = Aron.split(words, "\\s+");
                for(int i=0; i<listWord.size(); i++){
                    List<String> subList = listWord.subList(i, listWord.size());
                    String prefixKey = "";
                    for(String word : subList){
                        prefixKey = prefixKey + " " + word;
                        prefixKey = prefixKey.trim().toLowerCase();
                        Set<List<String>> set = mapSet.get(prefixKey);
                        if (set != null) {
                            set.add(listCode.subList(1, listCode.size()));
                            mapSet.put(prefixKey, set);
                        } else {
                            Set<List<String>> tmpSet = new HashSet<>();
                            tmpSet.add(listCode.subList(1, listCode.size()));
                            mapSet.put(prefixKey, tmpSet);
                        }
                        Print.pbl("------------------");
                    }
                }
                Print.pbl("==============");
            }
        }else{
            Print.pbl("ERROR: invalid file format. listCode.size()=" + listCode.size());
        }
        return mapSet;
    }

    // "vi cmd"
    // "v" -> "vi cmd"
    // "vi -> "vi cmd"
    // "vi "-> "vi cmd"
    // "vi c"-> "vi cmd"
    // "vi cm" -> "vi cmd"
    // "vi cmd" -> "vi cmd"
    //
    // "c" -> "cmd"
    // "cm" -> "cmd"
    // "cmd" -> "cmd"
    static void prefixStringMap(List<String> list, Map<String, Set<String>> map){
//        Map<String, Set<String>> map = new HashMap<>();

        for(int k=0; k<list.size(); k++) {
            String s = list.get(k);
            for(int i=0; i<s.length(); i++) {
                String key = s.substring(0, i + 1);
                Print.pbl("key=" + key);
                List<String> preList = list.subList(0, k);
                Print.pbl("->" + listToStr(preList));
                List<String> subList = list.subList(k, list.size());

                Set<String> value = map.get(key);
                if(value != null){
                    value.add(listToStr(subList));
                }else{
                    Set<String> set = new HashSet<>();
                    set.add(listToStr(subList));
                    map.put(key, set);
                }
            }
        }
        for(Map.Entry<String, Set<String>> entry : map.entrySet()){
            System.out.print("[" + entry.getKey() + "]->[" + entry.getValue() + "]");
            Print.line();
        }
//        return map;
    }

    static String listToStr(List<String> list){
        String retStr = "";
        for(String s : list)
            retStr += s + " ";

        return retStr.trim();
    }

}