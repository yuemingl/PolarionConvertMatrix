/*
    Copyright (c) 2014-2023 Matrix Requirements GmbH - https://matrixreq.com

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.   
*/

package com.matrixreq.polarionxmlconvert;

import com.opencsv.CSVReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Stack;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.select.Elements;
import org.w3c.dom.Node;

import com.matrixreq.client.matrixrestclient.MatrixRestClient;
import com.matrixreq.client.matrixrestclient.struct.CategoryAndItems;
import com.matrixreq.client.matrixrestclient.struct.Field;
import com.matrixreq.client.matrixrestclient.struct.FieldAndValue;
import com.matrixreq.client.matrixrestclient.struct.FieldAndValueList;
import com.matrixreq.client.matrixrestclient.struct.ItemAndSerial;
import com.matrixreq.lib.HtmlUtil;
import com.matrixreq.lib.StringUtil;
import com.matrixreq.xml.XDocument;
import com.matrixreq.xml.XElement;

/**
 *
 * @author Yves
 */
public class PolarionXmlConvert {

    public static final String REASON = "Polarion convert";
    
    /**
     * --instance=zbeats --project=ZB_CONV_13 --token=token --xml=ZBPro_Work_Items.xml --picFolder=pic --itemTracker=tracker.txt --category=PREQ --type=heading --steps=Steps --removeNumbers=1 --replaceDots=1
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            PolarionXmlConvert a = new PolarionXmlConvert();
            a.run(args);
        } catch (Exception ex) {
            System.out.println("Error: " + ex.getMessage());
            System.out.println("USAGE: java -jar PolarionXmlConvert.jar --instance=instance --project=project "
                              + "--token=token --xml=xmlInput.xml --picFolder=folder --globalItemTracker=file.txt "
                              +" [--type=itemType][--steps=fieldName][--relation=other1,other2][--removeNumbers=1][--replaceDots=1]");
            System.out.println("   The optional itemType argument is userRequirement to filter items by: <type id=\"userRequirement\" for example");
            System.out.println("   The optional steps is the name of the Matrix field containing the test steps");
            System.out.println("   The optional removeNumbers=1 is for the removal of chapters numbers in titles");
            System.out.println("   The optional replaceDots=1 is for the replacing titles with the first line of text if it contains ...");
            ex.printStackTrace();
        }
    }
    private String instance;
    private String token;
    private String project;
    //private String categoryShort;
    private String xmlInput;
    private String docInput;
    private String itemTracker;
    private String picFolder;
    private boolean removeNumbers;
    private boolean replaceDots;
    private MatrixRestClient cli;
    private XDocument docX;
    private Images images;
    private String itemTypeFilter = null;
    private String stepsField = null;
    private List<String> otherRelations = new ArrayList<>();

    public static class WorkItem {
        String moduleName;
        String workItemId;
        String type;
        String title;
        String outlineNumber;
        String description;

        XElement workItemX;

        public List<LinkedWorkItem> linkedItems = new ArrayList<>();
        public List<LinkedWorkItem> linkedItemsDerived = new ArrayList<>();

        public WorkItem(String moduleName, String workItemId, String type, String outlineNumber, String title, XElement workItemX) {
            this.moduleName = moduleName;
            this.workItemId = workItemId;
            this.type = type;
            this.title = title;
            this.outlineNumber = outlineNumber;
            this.workItemX = workItemX;
        }

        void WorkItem(String moduleName, String workItemId, String type, String title, String outlineNumber, String description, XElement workItemX) {
            this.moduleName = moduleName;
            this.workItemId = workItemId;
            this.type = type;
            this.title = title;
            this.outlineNumber = outlineNumber;
            this.description = description;
            this.workItemX = workItemX;
        }

        public String toString() {
            String ret = moduleName + " " + workItemId + " " + type + " " + outlineNumber + " " + title + "\n";
            for(LinkedWorkItem linkedWorkItem : linkedItems) {
                ret += "\t parent: " + linkedWorkItem.toString() + "\n";
            }
            for(LinkedWorkItem linkedWorkItem : linkedItemsDerived) {
                ret += "\t child: " + linkedWorkItem.toString() + "\n";
            }
            return ret;
        }
    }

    public static class LinkedWorkItem {
        String role;
        WorkItem workItem;
        LinkedWorkItem(String role, WorkItem workItem) {
            this.role = role;
            this.workItem = workItem;
        }
        public String toString() {
            return workItem.workItemId + " " + workItem.type + " " + workItem.outlineNumber + " " +workItem.title;
        }
    }

    private void run(String[] args) throws Exception {
        if (args.length < 3)
            throw new Exception("Not enough arguments");
        for (int arg = 0; arg < args.length; arg++) {
            String argS = args[arg];
            String before = StringUtil.before(argS, "=");
            String after = StringUtil.after(argS, "=");
            switch (before) {
                case "--instance":
                    instance = after;
                    break;
                case "--token":
                    token = "api_nlv5b9tqngjmuk8t2qja035l8m.dvjd1rv40gh9m7bu26vbh2c0n0"; //after;
                    break;
                case "--project":
                    project = after;
                    break;
                //case "--category":
                //    categoryShort = after;
                //    break;
                case "--xml":
                    xmlInput = after;
                    break;
                case "--doc":
                    docInput = after;
                    break;
                case "--itemTracker":
                    itemTracker = after;
                    break;
                case "--picFolder":
                    picFolder = after;
                    break;
                case "--type":
                    itemTypeFilter = after;
                    break;
                case "--steps":
                    stepsField = after;
                    break;
                case "--relation":
                    String[] split = StringUtil.splitOnString(after, ",");
                    otherRelations = StringUtil.arrayToArrayList(split);
                    break;
                case "--removeNumbers":
                    removeNumbers = "1".equals(after);
                    break;
                case "--replaceDots":
                    replaceDots = "1".equals(after);
                    break;
                default:
                    break;
            }
        }

        if (StringUtils.isEmpty(picFolder))
            throw new Exception("Missing picFolder argument");
        if (StringUtils.isEmpty(itemTracker))
            throw new Exception("Missing itemTracker argument");
        images = new Images();
        images.setPicFolder(picFolder);
        images.setItemTracker(itemTracker);
        if (StringUtils.isEmpty(xmlInput))
            throw new Exception("Missing xml argument");
        if (StringUtils.isEmpty(project))
            throw new Exception("Missing project argument");
        if (StringUtils.isEmpty(instance))
            throw new Exception("Missing instance argument");
        if (StringUtils.isEmpty(token))
            throw new Exception("Missing token argument");
        //if (StringUtils.isEmpty(categoryShort))
        //    throw new Exception("Missing category argument");
        readCSV();
        convert();
    }

    //private Map<String,Integer> matrixFields = new HashMap<>();

    ////////////////////////////////////////////////////////////////////////
    public static class FieldIDSet {
        private Integer descriptionFieldId = null;
        private Integer legacyIdFieldId = null;
        private Integer uplinksFieldId = null;
        private Integer stepsFieldId = null;
        private Integer upId = null;
        private Integer referenceId = null;
    }
    Map<String, FieldIDSet> mapCat2FieldIDs = new HashMap<>();

//    private Integer descriptionFieldId = null;
//    private Integer legacyIdFieldId = null;
//    private Integer uplinksFieldId = null;
//    private Integer stepsFieldId = null;
//    private Integer upId = null;
//    private Integer referenceId = null;
    ////////////////////////////////////////////////////////////////////////

    private Map<String,XElement> outlineNumbers = new HashMap<>();

    Map<String, String> mapID2ID = new HashMap<>();
    Map<String, WorkItem> mapWorkItems = new HashMap<>();
    Map<String, String> mapTypeToCat = new HashMap<>();

    int compareOutlineNumbers(String a, String b) {
        List<Integer> al = outlineToInts(a);
        List<Integer> bl = outlineToInts(b);
        int len = al.size() < bl.size() ? al.size() : bl.size();
        for(int i = 0; i < len; i++) {
            int ret = al.get(i) - bl.get(i);
            if(ret != 0) return ret;
        }
        return al.size() - bl.size();
    }

    List<LinkedWorkItem> getLinkedWorkItems(Map<String, WorkItem> mapWorkItems, XElement fieldsX) {
        List<LinkedWorkItem> ret = new ArrayList<>();
        if (fieldsX != null) {
            ArrayList<XElement> linkedItemsX = fieldsX.getAllChildrenElementLocal("linkedWorkItem");
            if (linkedItemsX != null) {
                for (XElement linkedItemX: linkedItemsX) {
                    String workItemId = linkedItemX.getFirstChildElementLocal("workItem").getAttribute("workItemId");
                    String role = linkedItemX.getFirstChildElementLocal("role").getAttribute("id");
                    WorkItem workItem = mapWorkItems.get(workItemId);
                    if(workItem != null) {
                        ret.add(new LinkedWorkItem(role, workItem));
                    }
                }
            }
        }
        return ret;
    }

    Map<String, String> mapID2Title = new HashMap<String, String>();

    void readCSV() {
        CSVReader csvReader = null;
        try {
            csvReader = new CSVReader(new FileReader("workItem_titles_v1.csv"));
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                mapID2Title.put(line[2], line[1]); //workItemID => tile
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void convert() throws Exception {
//        type id="architectureElement"    -> ARCH
//        type id="designElement"      -> SDD
//        type id="info"                    -> No need
//        type id="heading"             -> No need
//        type id="unitTestCase"       -> UTC
//        type id="softwareTestCase"     -> SWTC
//        type id="softwareRequirement"      ->SWREQ
//        type id="productRequirement"       -> PREQ
        mapTypeToCat.put("architectureElement","ARCH");
        mapTypeToCat.put("designElement","SDD");
        mapTypeToCat.put("unitTestCase","UTC");
        mapTypeToCat.put("softwareTestCase","SWTC");
        mapTypeToCat.put("softwareRequirement","SWREQ");
        mapTypeToCat.put("productRequirement","SYREQ"); //PREQ
        mapTypeToCat.put("productRequirement_XXX","PREQ");

        images.reloadAllImages();
        images.reloadAllWorkItems();
        docX = XDocument.loadXmlFile(xmlInput);
        cli = new MatrixRestClient(MatrixRestClient.fixInstance(instance) + "/rest/1");
        cli.setTokenAuthorization(token);

        for(String cat : mapTypeToCat.values()) {
            CategoryAndItems category = cli.getCategory(project, cat);
            FieldIDSet fields = new FieldIDSet();
            for (Field field : category.fieldList) {
                //matrixFields.put(field.label, field.id);
                switch (field.label) {
                    case "Description":
                        fields.descriptionFieldId = field.id;
                        break;
                    case "LegacyID":
                        fields.legacyIdFieldId = field.id;
                        break;
                    case "Uplinks":
                        fields.uplinksFieldId = field.id;
                        break;
                    case "Up":
                        fields.upId = field.id;
                        break;
                    case "References":
                        fields.referenceId = field.id;
                        break;
                    default:
                        // TODO no need
                        if (stepsField != null && field.label.equals(stepsField))
                            fields.stepsFieldId = field.id;
                        break;
                }
            }
            mapCat2FieldIDs.put(cat, fields);
        }

        List<WorkItem> workItems = new ArrayList<>();

        List<String> allOutline = new ArrayList<>();
        ArrayList<XElement> workItemListX = docX.getDocumentElement().getAllChildrenElementLocal("workItem");
        System.out.println("Found " + workItemListX.size() + " workItems");
        for (XElement workItemX: workItemListX) {
            // <outlineNumber>3.2-1</outlineNumber>
            XElement fieldsX = workItemX.getFirstChildElementLocal("fields");
            String outLineNumber = null;
            try {
                outLineNumber = fieldsX.getFirstChildElementLocal("outlineNumber").getText();
            }
            catch (Exception ex) {
                outLineNumber = null;
            }
            if (outLineNumber != null) {
                String moduleName = fieldsX.getFirstChildElementLocal("module").getText();
                String type = fieldsX.getFirstChildElementLocal("type").getAttribute("id");
                String title = fieldsX.getFirstChildElementLocal("title").getText();
                String workItemId = fieldsX.getFirstChildElementLocal("id").getText();
                workItems.add(new WorkItem(moduleName, workItemId, type, outLineNumber, title.replaceAll("^[0-9.-]*",""), workItemX));

                if (itemTypeFilter == null || itemTypeFilter.equals(type)) {
                    outlineNumbers.put(outLineNumber, workItemX);
                    allOutline.add(outLineNumber);
                }
            }
        }

        Collections.sort(workItems, (a, b) -> {
            int ret1 = a.moduleName.compareTo(b.moduleName);
            if(ret1 == 0) {
                return compareOutlineNumbers(a.outlineNumber, b.outlineNumber);
//                int ret2 =  a.type.compareTo(b.type);
//                if(ret2 == 0) {
//                    return compareOutlineNumbers(a.outlineNumber, b.outlineNumber);
//                }
//                return ret2;
            }
            return ret1;
        });

        for(WorkItem workItem : workItems) {
            mapWorkItems.put(workItem.workItemId, workItem);
        }

        for (XElement workItemX: workItemListX) {
            XElement fieldsX = workItemX.getFirstChildElementLocal("fields");
            String workItemId = fieldsX.getFirstChildElementLocal("id").getText();

            XElement linkedWorkItemsX = fieldsX.getFirstChildElementLocal("linkedWorkItems");
            List<LinkedWorkItem> parent = getLinkedWorkItems(mapWorkItems, linkedWorkItemsX);

            XElement linkedWorkItemsDerivedX = fieldsX.getFirstChildElementLocal("linkedWorkItemsDerived");
            List<LinkedWorkItem> children = getLinkedWorkItems(mapWorkItems, linkedWorkItemsDerivedX);

            WorkItem workItem = mapWorkItems.get(workItemId);
            if(workItem != null) {
                workItem.linkedItems.addAll(parent);
                workItem.linkedItemsDerived.addAll(children);
            } else {
                System.out.println(">>>>" + workItemId + " not in mapWorkItems");
            }
        }

        //for(WorkItem workItem : workItems) {
        //    System.out.println(workItem);
        //}
/**
        String rootFolder = "F-" + categoryShort + "-1";
        for(WorkItem workItem : workItems) {
            if(workItem.type.equals("heading")) {
                if(workItem.linkedItems.size() > 0) {
                    LinkedWorkItem parent = workItem.linkedItems.get(0);
                    String matrixFolder = mapID2ID.get(parent.workItem.workItemId);
                    if(matrixFolder != null) {

                        ItemAndSerial addFolder = cli.addFolder(project, matrixFolder,
                            workItem.outlineNumber + " " + workItem.title, REASON, null);
                        String newMatrixFolder = "F-" + categoryShort + "-" + addFolder.serial;
                        mapID2ID.put(workItem.workItemId, newMatrixFolder);
                    }
                } else {
                    String moduleFolder = mapModule2ID.get(workItem.moduleName);
                    // Create module folder in Matrix
                    if(moduleFolder == null) {
                        ItemAndSerial addFolder = cli.addFolder(project, rootFolder,
                            workItem.moduleName, REASON, null);
                        String newMatrixFolder = "F-" + categoryShort + "-" + addFolder.serial;
                        mapModule2ID.put(workItem.moduleName, newMatrixFolder);
                        moduleFolder = newMatrixFolder;
                    }
                    ItemAndSerial addFolder = cli.addFolder(project, moduleFolder,
                        workItem.outlineNumber + " " + workItem.title, REASON, null);
                    String newMatrixFolder = "F-" + categoryShort + "-" + addFolder.serial;
                    System.out.println("Folder created: " + newMatrixFolder);
                    mapID2ID.put(workItem.workItemId, newMatrixFolder);
                }
            } else {
                if(workItem.linkedItems.size() > 0) {
                    for(LinkedWorkItem parent : workItem.linkedItems) {
                        if(parent.role.equals("parent") && parent.workItem.type.equals("heading")) {
                            String parentMatrixFolder = mapID2ID.get(parent.workItem.workItemId);
                            if(parentMatrixFolder != null) {
                                convertOne(workItem.workItemX, parentMatrixFolder, false);
                            } else {
                                System.out.println("(Create) NO MATRIX PARENT FOLDER: " + workItem);
                            }
                            break;
                        }
                    }
                } else {
                    System.out.println("(Create) NO linkedItems: " + workItem);
                }
            }
        }
*/

// Print to stdout for a CSV file format to fix titles manually
//        for(WorkItem workItem : workItems) {
//            if (workItem.type.equals("heading") || workItem.type.equals("info"))
//                continue;
//            printDesc(workItem.workItemX);
//        }

        for(WorkItem workItem : workItems) {
            try {
                if (workItem.type.equals("heading") || workItem.type.equals("info"))
                    continue;
                if (workItem.linkedItems.size() > 0) {
                    Stack<WorkItem> parentStack = new Stack<>();
                    WorkItem currentWorkItem = workItem;
                    while (true) {
                        boolean isRoot = true;
                        for (LinkedWorkItem parent : currentWorkItem.linkedItems) {
                            if (parent.role.equals("parent") &&
                                parent.workItem.type.equals("heading")) {
                                parentStack.push(parent.workItem);
                                currentWorkItem = parent.workItem;
                                isRoot = false;
                                break;
                            }
                        }
                        if (isRoot) break;
                    }

                    // Create folder from root to the immediate parent if needed
                    String currentCategory = mapTypeToCat.get(workItem.type);
                    String rootMatrixFolder = "F-" + currentCategory + "-1";
                    if (currentCategory != null) {
                        WorkItem immediateParent = null;
                        while (!parentStack.isEmpty()) {
                            WorkItem parent = parentStack.pop();
                            if (parentStack.isEmpty()) {
                                immediateParent = parent;
                            }
                            // Check if already created
                            String parentMatrixFolder = mapID2ID.get(parent.workItemId);
                            if (parentMatrixFolder == null) {
                                // hike
                                if(currentCategory.equals("SYREQ") && parent.outlineNumber.equals("2")) {
                                    currentCategory = "PREQ";
                                    rootMatrixFolder = "F-PREQ-1";
                                }
                                ItemAndSerial addFolder = cli.addFolder(project, rootMatrixFolder,
                                    //parent.outlineNumber + " " +
                                        parent.title, REASON, null);
                                String newMatrixFolder =
                                    "F-" + currentCategory + "-" + addFolder.serial;
                                System.out.println("Folder created: " + newMatrixFolder);
                                mapID2ID.put(parent.workItemId, newMatrixFolder);
                                rootMatrixFolder = newMatrixFolder;
                            } else {
                                rootMatrixFolder = parentMatrixFolder;
                            }
                        }
                        if (immediateParent != null) {
                            if(currentCategory.equals("SYREQ") && mapWorkItems.get(immediateParent.workItemId).outlineNumber.equals("2")) {
                                currentCategory = "PREQ";
                            }
                            String parentMatrixFolder = mapID2ID.get(immediateParent.workItemId);
                            convertOne(workItem.workItemX, currentCategory, parentMatrixFolder, false);
                        } else {
                            System.out.println("immediateParent is null: " + workItem);
                        }
                    } else {
                        System.out.println("No category found: " + workItem);
                    }
                } else {
                    System.out.println("(Create) NO linkedItems: " + workItem);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

/**
        for(WorkItem workItem : workItems) {
            if(workItem.type.equals("heading"))
                continue;
            if(workItem.linkedItems.size() > 0) {
                for(LinkedWorkItem parent : workItem.linkedItems) {
                    if(parent.role.equals("parent") && parent.workItem.type.equals("heading")) {
                        String parentMatrixFolder = mapID2ID.get(parent.workItem.workItemId);
                        if(parentMatrixFolder != null) {
                            convertOne(workItem.workItemX, parentMatrixFolder, true); // Update for linked items
                        } else {
                            System.out.println("(Update) NO MATRIX PARENT FOLDER: " + workItem);
                        }
                        break;
                    }
                }
            } else {
                System.out.println("(Update) NO linkedItems: " + workItem);
            }
        }
*/
/**
        Collections.sort(allOutline, new CustomComparator());
        String main = "F-" + categoryShort + "-1";
        folderToMatrix.put("", main);
        for (String workItem: allOutline) {
            XElement workItemX = outlineNumbers.get(workItem);
            System.out.println("Item : " + workItem);
            String header = getHeaderFromOutline(workItem, 2);
            String matrixFolder = folderToMatrix.get(header);
            if (matrixFolder == null) {
                // We'll have to create a new one
                int level = outlineToInts(workItem).size();
                String prevLevel = main;
                for (int check = 1; check <= level - 2; check++) {
                    String upperHeader = getHeaderFromOutline(workItem, level - check);
                    String existing = folderToMatrix.get(upperHeader);
                    if (existing == null) {
                        ItemAndSerial addFolder = cli.addFolder(project, prevLevel, upperHeader, REASON, null);
                        String newMatrixFolder = "F-" + categoryShort + "-" + addFolder.serial;
                        System.out.println("Created new folder " + newMatrixFolder + " for " + upperHeader + " under " + prevLevel);
                        folderToMatrix.put(upperHeader, newMatrixFolder);
                        prevLevel = newMatrixFolder;
                    }
                    else   
                        prevLevel = existing;
                }
                matrixFolder = prevLevel;
            }
            System.out.println("Will create " + workItem + " under " + matrixFolder);
            convertOne(workItemX, matrixFolder);
        }
*/
    }

    private Map<String,String> folderToMatrix = new HashMap<>();

    private String getHeaderFromOutline(String workItem, int minus) {
        List<Integer> outlineToInts = outlineToInts(workItem);
        String ret = "";
        for (int i = 0; i < outlineToInts.size() - minus; i++)
            ret += "." + outlineToInts.get(i);
        if (StringUtils.isEmpty(ret))
            return "";
        return ret.substring(1);
    }

    public class CustomComparator implements Comparator<String> {
        @Override
        public int compare(String o1, String o2) {
            List<Integer> l1 = outlineToInts(o1);
            List<Integer> l2 = outlineToInts(o2);
            int size = Integer.min(l1.size(), l2.size());
            for (int part = 0; part < size; part++)
                if (l1.get(part)!=l2.get(part))
                    return l1.get(part) - l2.get(part);
            return l2.size() - l1.size();
        }
    }    

    // converts 3.2-1 to [3,2,1]
    private List<Integer> outlineToInts(String input) {
        input = input.replace(".","-");
        String[] split = StringUtil.splitOnString(input, "-");
        List<Integer> ret = new ArrayList<>();
        for (String s: split)
            ret.add(StringUtil.stringToIntZero(s));
        return ret;
    }

    List<String> workItemId2MatrixId(List<String> workItemIds) {
        ArrayList<String> ret = new ArrayList<>();
        for(String workItemId : workItemIds) {
            String mId = mapID2ID.get(workItemId);
            if(mId != null) {
                ret.add(mId);
            }
        }
        return ret;
    }


    private void printDesc(XElement workItemX) throws Exception {
        XElement fieldsX = workItemX.getFirstChildElementLocal("fields");
        XElement idX = fieldsX.getFirstChildElementLocal("id");
        String legacyId = idX.getText();
        String type = fieldsX.getFirstChildElementLocal("type").getAttribute("id");
        String cat = mapTypeToCat.get(type);

        XElement titleX = fieldsX.getFirstChildElementLocal("title");
        String title = titleX.getText();

        XElement descriptionX = fieldsX.getFirstChildElementLocal("description");
        String htmlDescription = "";
        if(descriptionX != null) {
            htmlDescription = nodeToString(descriptionX.getUnderlyingElement());
            htmlDescription = htmlDescription.replace("<description xmlns=\"http://polarion.com/xml-export\">", "")
                .replace("</description>", "")
                .replace("xmlns=\"http://www.w3.org/1999/xhtml\"", "")
                .replace("<html>", "")
                .replace("</html>", "")
                .replace("\"", "")
                .replace("\r", "")
                .replace("\n", "")
                .trim();
        }
        if (removeNumbers)
            title = removeNumbers(title);
        if (title.contains("...") && replaceDots)
            title = replaceDots(title, htmlDescription);
        System.out.print("\"" + cat + "\",");
        System.out.print("\"" + legacyId + "\",");
        System.out.print("\"" + title.trim().replace("[.,\\s]$","") + "\",");
        System.out.print("\"" + htmlDescription + "\",");
        System.out.println();
    }

    private void convertOne(XElement workItemX, String category, String matrixFolder, boolean update) throws Exception {
        XElement fieldsX = workItemX.getFirstChildElementLocal("fields");
        /*
          <id>ZBS01-23884</id>
         */
        XElement idX = fieldsX.getFirstChildElementLocal("id");
        String legacyId = idX.getText();

        boolean rejected = false;
        rejected = ! images.getWorkItems().contains(legacyId);

        /*
         <title>4.1.1-1 Logging with SLF4J Logger: The Simple Logging Facade for Java (SLF4J) is used as...</title>
         */
        XElement titleX = fieldsX.getFirstChildElementLocal("title");
        String title = titleX.getText();
        if (removeNumbers)
            title = removeNumbers(title);
        /*
            <type id="designElement" name="Design Element" isDefault="false" sequenceNumber="38">
                <property id="iconURL">/polarion/icons/default/enums/type_requirement.gif</property>
            </type>             
         */
        String type = fieldsX.getFirstChildElementLocal("type").getAttribute("id");

        /*
         <description>
            <html>
                <span xmlns="http://www.w3.org/1999/xhtml" style="font-weight: bold;">Logging with SLF4J Logger:</span>
                <br xmlns="http://www.w3.org/1999/xhtml"/>
                <span xmlns="http://www.w3.org/1999/xhtml" style="font-size: 11pt;color: #000000;line-height: 1.5;">The Simple Logging Facade for Java (SLF4J) is used as the logging framework. It allows the ZBPro system to log all API fails (errors and warnings) at the deployment time and log additional debug messages as development time. All the API error and warning logs will persist on the backend file systems and can be accessed by only authorized users.</span>
            </html>
         </description>
         */
        XElement descriptionX = fieldsX.getFirstChildElementLocal("description");
        String htmlDescription = "";
        if(descriptionX != null) {
            htmlDescription = nodeToString(descriptionX.getUnderlyingElement());
            htmlDescription = htmlDescription.replace("<description xmlns=\"http://polarion.com/xml-export\">", "")
                .replace("</description>", "")
                .replace("xmlns=\"http://www.w3.org/1999/xhtml\"", "")
                .replace("<html>", "")
                .replace("</html>", "")
                .trim();
            if (title.contains("...") && replaceDots)
                title = replaceDots(title, htmlDescription);
        }

        //System.out.println(type + " " + legacyId + ": " + title);
        //System.out.println();
        //System.out.println(htmlDescription);
        //System.out.println();
        XElement linkedX = fieldsX.getFirstChildElementLocal("linkedWorkItems");

        /*
            <linkedWorkItems>
            <linkedWorkItem>
            <revision>false</revision>
            <role id="implements" name="implements" isDefault="false" sequenceNumber="4">
                <property id="parent">true</property>
                <property id="description">Used for link implementing Tasks and Issues to Work Packages and also Work Packages to Requirements.</property>
                <property id="oppositeName">is implemented by</property>
            </role>
            <suspect>false</suspect>
            <workItem workItemId="ZBS01-22937" projectId="zbs01"/>
            </linkedWorkItem>            
         */
        List<String> upLinks = new ArrayList<>();
        List<String> upIds = new ArrayList<>();
        List<String> referenceIds = new ArrayList<>();
        if (linkedX != null) {
            ArrayList<XElement> linkedItemsX = linkedX.getAllChildrenElementLocal("linkedWorkItem");
            if (linkedItemsX != null) {
                for (XElement linkedItemX: linkedItemsX) {
                    String workItemId = linkedItemX.getFirstChildElementLocal("workItem").getAttribute("workItemId");
                    String role = linkedItemX.getFirstChildElementLocal("role").getAttribute("id");
                    //System.out.println("\t" + role + " " + workItemId);
                    //if ("parent".equals(role) && "ZBS01-24510".equals(workItem)) {
                    //    System.out.println("Gotcha");
                    //}
                    if(role.equals("parent")) {
                        upIds.add(workItemId);
                    } else {
                        referenceIds.add(workItemId);
                    }
                    switch (role) {
                        case "implements":
                        case "refines":
                        case "verifies":
                        case "depends_on":
                            upLinks.add(workItemId);
                            break;
                        default:
                            if (otherRelations != null && otherRelations.contains(role)) {
                                upLinks.add(workItemId);
                            }
                            break;
                    }
                }
            }
        }

        String testField = null;
        htmlDescription = uploadPictures(htmlDescription);
        FieldIDSet fields = mapCat2FieldIDs.get(category);
        if (fields.stepsFieldId != null) {
            String[] split = splitTest(htmlDescription);
            htmlDescription = split[0];
            testField = split[1];
        }

        FieldAndValueList fvl = new FieldAndValueList();
        fvl.fieldVal.add(new FieldAndValue(fields.descriptionFieldId, htmlDescription));
        if (fields.legacyIdFieldId != null)
            fvl.fieldVal.add(new FieldAndValue(fields.legacyIdFieldId, legacyId));
        if (fields.uplinksFieldId != null && ! upLinks.isEmpty())
            fvl.fieldVal.add(new FieldAndValue(fields.uplinksFieldId, StringUtil.joinArrayWith(upLinks, ",")));
        if (fields.stepsFieldId != null && StringUtils.isNotEmpty(testField))
            fvl.fieldVal.add(new FieldAndValue(fields.stepsFieldId, testField));
//        if (upId != null)
//            fvl.fieldVal.add(new FieldAndValue(upId, StringUtil.joinArrayWith(upIds, ",")));
//        if (referenceId != null)
//            fvl.fieldVal.add(new FieldAndValue(referenceId, StringUtil.joinArrayWith(workItemId2MatrixId(referenceIds), ",")));

        String titleFromCSV = mapID2Title.get(legacyId);
        if(titleFromCSV != null) {
            title = titleFromCSV;
        }
        ArrayList<String> labels = new ArrayList<>();
        if (rejected)
            labels.add("rejected");
        if(update) {
            String itemId = mapID2ID.get(legacyId);
            if(itemId != null) {
                String updateItem = cli.updateItem(project, itemId, title, REASON, fvl, labels);
                System.out.println("Item updated: " + itemId);
            } else {
                System.out.println("Cannot find matrix id for " + legacyId + " when updating");
            }

        } else {
            System.out.println("Adding " + legacyId + " to " + matrixFolder);
            ItemAndSerial addItem = cli.addItem(project, matrixFolder, title, REASON, fvl, "", labels);
            String addItemId = category + "-" + addItem.serial;
            mapID2ID.put(legacyId, addItemId);
            System.out.println("Item created: " + addItemId);
        }
    }

    private String replaceDotsFromString(String text, String defaultTitle) {
        if (StringUtils.isEmpty(text))
            return defaultTitle;
        int firstDot = text.indexOf(".");
        int firstColon = text.indexOf(":");
        // some text start with a function name module::function does this and that
        while (firstColon > 0 && firstColon < text.length() - 1 && text.charAt(firstColon + 1)==':')
            firstColon = text.indexOf(":", firstColon + 2);
        Integer cut = null;
        if (firstDot > 0) {
            if (firstColon > 0) {
                cut = Integer.min(firstDot, firstColon);
            }
            else {
                cut = firstDot;
            }
        }
        else {
            if (firstColon > 0)
                cut = firstColon;
        }
        if (cut != null) {
            String newTitle = text.substring(0, cut);
            return newTitle;
        }
        return text;
    }

    private String replaceDots(String title, String htmlDescription) {
        org.jsoup.nodes.Document htmlDoc = HtmlUtil.parseHtml(htmlDescription);

        // If the description starts with a table (but there may be +- empty text before), we'll extract from 1st cell
        Elements children = htmlDoc.body().children();
        String rawOther = htmlDoc.text().trim();
        if (! children.isEmpty()) {
            boolean stopTableSearch = false;
            for (int child = 0; child < 3 && ! stopTableSearch; child++) {
                if (child < children.size()) {
                    if (children.get(child).tagName().equals("table")) {
                        Elements select = children.get(child).select("td");
                        if (! select.isEmpty()) {
                            String rawFirstCell = select.get(0).text().trim();
                            if (StringUtils.isNotEmpty(rawFirstCell)) {
                                String newTitle = replaceDotsFromString(rawFirstCell, title);
                                return newTitle;
                            }
                        }
                    } 
                    else {
                        rawOther = children.get(child).text().trim();
                        if (StringUtils.isNotEmpty(rawOther))
                            stopTableSearch = true;
                    }
                }
            }
        }
        //String rawText = htmlDoc.text().trim();
        String newTitle = replaceDotsFromString(rawOther, title);
        return newTitle;
    }

    private String removeNumbers(String title) {
        if (! Character.isDigit(title.charAt(0)))
            return title;
        int index = title.indexOf(" ");
        if (index > 0 && index < 20) {
            String end = title.substring(1 + index).trim();
            if (StringUtils.isNotEmpty(end))
                return end;
        }
        return title;
    }

    /*
        Takes out the table containing the test steps and fills in a field for tests
        We assume the HTML contains this header:
        Step | Procedure | Expected Result | Result | Comments

        We also assume the test layer in Matrix has action and expected per this:
        "HWTC": {
            "columns": [
                { "name": "Action", "field": "action", "editor": "text" },
                { "name": "Expected Result", "field": "expected", "editor": "text" }
            ]
    },

    */
    private String[] splitTest(String htmlDescription) {
        String updatedDesc = htmlDescription;
        String test = "";

        org.jsoup.nodes.Document htmlDoc = HtmlUtil.parseHtml(htmlDescription);
        org.jsoup.select.Elements tablesH = htmlDoc.getElementsByTag("table");
        for (org.jsoup.nodes.Element table: tablesH) {
            org.jsoup.select.Elements select = table.select("th");
            if (select != null && ! select.isEmpty()) {
                String firstCell = select.get(0).text();
                if ("Step".equals(firstCell)) {
                    List<String> steps = new ArrayList<>();
                    org.jsoup.select.Elements stepLines = table.select("tr");
                    if (stepLines != null) {
                        for (org.jsoup.nodes.Element line: stepLines) {
                            org.jsoup.select.Elements cells = line.select("td");
                            if (cells != null && cells.size() > 2) {
                                // we get 2nd and 3rd column (1st is the step number that we don't need)
                                String text1 = cells.get(1).html();
                                String text2 = cells.get(2).html();
                                TestLine tl = new TestLine(text1, text2);
                                steps.add(tl.getJson());
                            }
                        }
                    }
                    // each string is a json object, we just need to create the array structure around
                    test = "[" + StringUtil.joinArrayWith(steps, ",") + "]";
                    table.remove();
                    updatedDesc = htmlDoc.html();
                    return new String[]{updatedDesc, test};
                }
            } 
        }
        // I couldn't find the step table
        return new String[]{htmlDescription, null};
    }
    private int imageCount = 1;

    /*
     * We look inside the htmlDescription for the URLs to the images. When found, we extract the image from the doc, upload it to matrix, and replace the URL with the one from Matrix
     */
    private String uploadPictures(String htmlDescription) throws Exception {
        /*
        for (String url: shapesFromUrl.keySet()) {
            if (htmlDescription.contains(url)) {
                System.out.println("Picture found: " + url);
                String imageFile = "picture"  + imageCount + ".png";
                storeShapeToFile(shapesFromUrl.get(url), imageFile);
                FileAndKey upload = cli.uploadFile(new File(imageFile), project);
                String newUrl = cli.getBaseUrl() + "/" + project + "/file/" + upload.fileId + "?key=" + upload.key;
                htmlDescription = htmlDescription.replace(url, newUrl);
                System.out.println("\t--> " + newUrl);
            }
        }
        */
        return images.convertImages(cli, project, htmlDescription);
        
    }

    // https://stackoverflow.com/questions/8979851/java-how-to-extract-a-complete-xml-block
    private static String nodeToString(Node node)    throws TransformerException
    {
        StringWriter buf = new StringWriter();
        Transformer xform = TransformerFactory.newInstance().newTransformer();
        xform.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        xform.setOutputProperty(OutputKeys.METHOD, "html");
        xform.transform(new DOMSource(node), new StreamResult(buf));
        return(buf.toString());
    }    

}
