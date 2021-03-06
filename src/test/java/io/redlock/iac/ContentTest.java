package io.redlock.iac;

import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ContentTest {

  private static final String rulesDirPath = "./src/main/resources/content";
  private static final String resourceExtension = "json";

  @Test
  public void ensureFileExtensionIsJson() throws Exception {
    for (TemplateType templateType : TemplateType.values()) {
      ensureFileExtensionIsJson(getResourcePathByTemplateType(templateType));
    }
  }

  @Test
  public void ensureRequiredFieldExistsCFTRules() throws Exception {
    ensureRequiredFieldExistsForTemplateType(TemplateType.CFT);
  }

  @Test
  public void ensureRequiredFieldExistsTFRules() throws Exception {
    ensureRequiredFieldExistsForTemplateType(TemplateType.TF);
  }

  @Test
  public void ensureRequiredFieldExistsK8SRules() throws Exception {
    ensureRequiredFieldExistsForTemplateType(TemplateType.K8S);
  }

  @Test
  public void validateIdUniqueness() throws Exception {
    for (TemplateType templateType : TemplateType.values()) {
      validateIdUniquenessForTemplateType(templateType);
    }
  }

  private void validateIdUniquenessForTemplateType(TemplateType templateType) throws Exception {
    Map<String, Rule> rulesByFiles = getRulesByTemplateType(getResourcePathByTemplateType(templateType));
    List<String> idList = new ArrayList<>();
    rulesByFiles.forEach((file,rule) -> idList.add(rule.getId()));
    Set<String> duplicates = findDuplicates(idList);
    if (duplicates.size() != 0) {
      Assert.fail("Found duplicates : " + duplicates);
    }
  }

  private void ensureRequiredFieldExistsForTemplateType(TemplateType templateType) throws Exception {

    Map<String, Rule> rulesByFiles = getRulesByTemplateType(getResourcePathByTemplateType(templateType));

    rulesByFiles.forEach((file, rule) -> {
      if (isEmpty(rule.getId())) {
        Assert.fail("id not defined in file: "+ file + ", rule: " + rule.toString());
      }

      if (isEmpty(rule.getPolicy())) {
        Assert.fail("policy not defined in file: "+ file + ", rule: " + rule.toString());
      }

      if (isEmpty(rule.getDescription())) {
        Assert.fail("Description not defined in file: "+ file + ", in rule: " + rule.toString());
      }

      if (isEmpty(rule.getSeverity())) {
        Assert.fail("Severity not defined in file: "+ file + ", in rule: " + rule.toString());
      }

      if (isEmpty(rule.getRule())) {
        Assert.fail("rule not defined in file: "+ file + ", in rule: " + rule.toString());
      }

      if(isEmpty(rule.getResourceType())) {
        Assert.fail("resourceType not defined in file: "+ file + ", in rule: " + rule.toString());
      }

    });
  }

  private String getResourcePathByTemplateType(TemplateType templateType) throws Exception {
    String resourcePath = "";
    if (templateType != null) {
      switch (templateType) {
        case CFT:
          resourcePath = rulesDirPath + "/" + TemplateType.CFT.getTemplateType();
          break;
        case K8S:
          resourcePath = rulesDirPath + "/" + TemplateType.K8S.getTemplateType();
          break;
        case TF:
          resourcePath = rulesDirPath + "/" + TemplateType.TF.getTemplateType();
          break;
      }
    }
    return resourcePath;
  }

  private Map<String, Rule> getRulesByTemplateType(String resourcePath) throws Exception {
    File contentDir = new File(resourcePath);
    Collection<File> files = FileUtils.listFiles(contentDir, new String[] {resourceExtension}, Boolean.TRUE);
    String content;

    Map<String, Rule> rulesByFiles = new HashMap<>();
    for (File eachJsonFile : files) {
      content = new String(Files.readAllBytes(Paths.get(eachJsonFile.getCanonicalPath())));
      JsonObject jsonObject = JsonParser.object().from(content);
      rulesByFiles.put(eachJsonFile.getCanonicalPath(), Rule.toRule(jsonObject));
    }
    return rulesByFiles;
  }

  private boolean isEmpty(String str) {
    return (str == null || str.isEmpty());
  }

  private <T> Set<T> findDuplicates(Collection<T> collection) {

    Set<T> duplicates = new LinkedHashSet<>();
    Set<T> uniques = new HashSet<>();

    for (T t : collection) {
      if (!uniques.add(t)) {
        duplicates.add(t);
      }
    }
    return duplicates;
  }

  private void ensureFileExtensionIsJson(String resourcePath) throws IOException {
    File contentDir = new File(resourcePath);
    Collection<File> files = FileUtils.listFiles(contentDir, null, Boolean.TRUE);
    for (File file : files) {
      String fileName= file.getCanonicalPath();
      if(file.getCanonicalPath().endsWith(".json") == false){
        Assert.fail("file extension should be json for file: "+ file.getName());
      }
    }
  }

}