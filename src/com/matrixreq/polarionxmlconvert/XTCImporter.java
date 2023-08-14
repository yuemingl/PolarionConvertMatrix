package com.matrixreq.polarionxmlconvert;

import com.matrixreq.client.matrixrestclient.MatrixRestClient;
import com.matrixreq.client.matrixrestclient.struct.CategoryAndItems;
import com.matrixreq.client.matrixrestclient.struct.Field;
import com.matrixreq.client.matrixrestclient.struct.FieldAndValue;
import com.matrixreq.client.matrixrestclient.struct.ItemAndValue;
import com.matrixreq.lib.MatrixLibException;
import org.codehaus.jackson.map.ObjectMapper;

public class XTCImporter {
  public static void main(String[] args) throws Exception {
    String token = "api_nlv5b9tqngjmuk8t2qja035l8m.dvjd1rv40gh9m7bu26vbh2c0n0"; //after;
    String instance = "zbeats";
    String project = "ZB_diagnostic_TEST";
    String category = "XTC";

    MatrixRestClient cli = new MatrixRestClient(MatrixRestClient.fixInstance(instance) + "/rest/1");
    cli.setTokenAuthorization(token);
    CategoryAndItems categoryAndItems = cli.getCategory(project, category);

    PolarionXmlConvert.FieldIDSet fields = new PolarionXmlConvert.FieldIDSet();
    for (Field field : categoryAndItems.fieldList) {
      System.out.println(field.label);
    }
    String item = "XTC-13";
    ItemAndValue itemAndValue = cli.getItem(project, item);
    System.out.println(itemAndValue);
    FieldAndValue fieldAndValue = itemAndValue.fieldValList.fieldVal.get(3);
    //[{"action":"action1","expected":"expected1","actual_results":"w","result":"p","comment":"ttt","human":"passed","render":"ok"}]
    ObjectMapper mapper = new ObjectMapper();
    TestCaseStep testCaseStep = new TestCaseStep();
    testCaseStep.action = "action12";
    testCaseStep.expected = "expected12";
    testCaseStep.actual_results = "www";
    testCaseStep.result = "p";
    testCaseStep.comment = "test";


    fieldAndValue.value = "["+mapper.writeValueAsString(testCaseStep)+"]";

    cli.updateItem(project, item, itemAndValue.title, "test purpose", itemAndValue.fieldValList, itemAndValue.labels);
  }

  static class TestCaseStep {
    public String action;
    public String expected;
    public String actual_results;
    public String result;
    public String comment;
    //public String human;
    //public String render;
  }


}
