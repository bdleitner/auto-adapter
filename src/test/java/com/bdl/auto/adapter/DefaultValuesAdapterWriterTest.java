package com.bdl.auto.adapter;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Map;

/**
 * Tests for the {@linkplain DefaultValuesAdapterWriter} class.
 *
 * @author Ben Leitner
 */
@RunWith(JUnit4.class)
public class DefaultValuesAdapterWriterTest {

  private static final TypeMetadata VOID = TypeMetadata.builder()
      .setName("void")
      .build();

  private static final TypeMetadata INT = TypeMetadata.builder()
      .setName("int")
      .build();
  private static final TypeMetadata STRING = TypeMetadata.builder()
      .setPackageName("java.lang")
      .setName("String")
      .build();
  private static final TypeMetadata THING = TypeMetadata.builder()
      .setPackageName("com.bdl.auto.adapter")
      .setName("Thing")
      .build();

  @Test
  public void testSimpleClass() throws Exception {
    ClassMetadata type = ClassMetadata.builder()
        .setCategory(ClassMetadata.Category.CLASS)
        .setType(TypeMetadata.builder()
            .setPackageName("com.bdl.auto.adapter")
            .setName("Simple")
            .build())
        .addAbstractMethod(
            MethodMetadata.builder()
                .setVisibility(Visibility.PUBLIC)
                .setType(INT)
                .setName("add")
                .addParameter(ParameterMetadata.of(INT, "first"))
                .addParameter(ParameterMetadata.of(INT, "second"))
                .build())
        .addAbstractMethod(
            MethodMetadata.builder()
                .setVisibility(Visibility.PUBLIC)
                .setType(STRING)
                .setName("repeat")
                .addParameter(ParameterMetadata.of(STRING, "template"))
                .addParameter(ParameterMetadata.of(INT, "times"))
                .build())
        .addAbstractMethod(
            MethodMetadata.builder()
                .setVisibility(Visibility.PUBLIC)
                .setType(THING)
                .setName("getThing")
                .build())
        .build();

    assertOutput(type);
  }

  @Test
  public void testSimpleInterface() throws Exception {
    ClassMetadata type = ClassMetadata.builder()
        .setCategory(ClassMetadata.Category.INTERFACE)
        .setType(TypeMetadata.builder()
            .setPackageName("com.bdl.auto.adapter")
            .setName("Simple")
            .build())
        .addAbstractMethod(
            MethodMetadata.builder()
                .setVisibility(Visibility.PUBLIC)
                .setType(INT)
                .setName("add")
                .addParameter(ParameterMetadata.of(INT, "first"))
                .addParameter(ParameterMetadata.of(INT, "second"))
                .build())
        .addAbstractMethod(
            MethodMetadata.builder()
                .setVisibility(Visibility.PUBLIC)
                .setType(VOID)
                .setName("modify")
                .addParameter(ParameterMetadata.of(STRING, "input"))
                .build())
        .build();

    assertOutput(type);
  }

  @Test
  public void testHasAnImplementedMethod() throws Exception {
    MethodMetadata addMethod = MethodMetadata.builder()
        .setVisibility(Visibility.PUBLIC)
        .setType(INT)
        .setName("add")
        .addParameter(ParameterMetadata.of(INT, "first"))
        .addParameter(ParameterMetadata.of(INT, "second"))
        .build();
    MethodMetadata subtractMethod = MethodMetadata.builder()
        .setVisibility(Visibility.PUBLIC)
        .setType(INT)
        .setName("subtract")
        .addParameter(ParameterMetadata.of(INT, "first"))
        .addParameter(ParameterMetadata.of(INT, "second"))
        .build();
    ClassMetadata type = ClassMetadata.builder()
        .setCategory(ClassMetadata.Category.CLASS)
        .setType(TypeMetadata.builder()
            .setPackageName("com.bdl.auto.adapter")
            .setName("Partial")
            .build())
        .addAbstractMethod(addMethod)
        .addAbstractMethod(subtractMethod)
        .addImplementedMethod(addMethod)
        .build();

    assertOutput(type);
  }

  @Test
  public void testHasConstructors() throws Exception {
    ClassMetadata type = ClassMetadata.builder()
        .setCategory(ClassMetadata.Category.CLASS)
        .setType(TypeMetadata.builder()
            .setPackageName("com.bdl.auto.adapter")
            .setName("Constructable")
            .build())
        .addConstructor(ConstructorMetadata.builder()
            .visibility(Visibility.PUBLIC)
            .addParameter(ParameterMetadata.of(INT, "arg1"))
            .addParameter(ParameterMetadata.of(STRING, "arg2"))
            .build())
        .addConstructor(ConstructorMetadata.builder()
            .visibility(Visibility.PACKAGE_LOCAL)
            .addParameter(ParameterMetadata.of(STRING, "arg1"))
            .build())
        .addAbstractMethod(MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setType(INT)
            .setName("add")
            .addParameter(ParameterMetadata.of(INT, "first"))
            .addParameter(ParameterMetadata.of(INT, "second"))
            .build())
        .addAbstractMethod(MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setType(INT)
            .setName("subtract")
            .addParameter(ParameterMetadata.of(INT, "first"))
            .addParameter(ParameterMetadata.of(INT, "second"))
            .build())
        .build();

    assertOutput(type);
  }

  @Test
  public void testNested() throws Exception {
    ClassMetadata type = ClassMetadata.builder()
        .setCategory(ClassMetadata.Category.CLASS)
        .setType(TypeMetadata.builder()
            .setPackageName("com.bdl.auto.adapter")
            .setName("Inner")
            .addOuterClass("Outer")
            .addOuterClass("Super")
            .build())
        .addConstructor(ConstructorMetadata.builder()
            .visibility(Visibility.PUBLIC)
            .addParameter(ParameterMetadata.of(INT, "arg1"))
            .addParameter(ParameterMetadata.of(STRING, "arg2"))
            .build())
        .addAbstractMethod(MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setType(INT)
            .setName("add")
            .addParameter(ParameterMetadata.of(INT, "first"))
            .addParameter(ParameterMetadata.of(INT, "second"))
            .build())
        .addAbstractMethod(MethodMetadata.builder()
            .setVisibility(Visibility.PUBLIC)
            .setType(INT)
            .setName("subtract")
            .addParameter(ParameterMetadata.of(INT, "first"))
            .addParameter(ParameterMetadata.of(INT, "second"))
            .build())
        .build();

    assertOutput(type);
  }

  private void assertOutput(ClassMetadata type) throws Exception {
    final Map<String, Writer> writerMap = Maps.newHashMap();

    DefaultValuesAdapterWriter writer = new DefaultValuesAdapterWriter(
        new Function<String, Writer>() {
          @Override
          public Writer apply(String input) {
            StringWriter writer = new StringWriter();
            writerMap.put(input + ".txt", writer);
            return writer;
          }
        });

    String key = String.format("%s.%s.txt", type.type().packageName(), type.decoratedName("DefaultValues"));
    writer.write(type);

    URL resource = getClass().getClassLoader().getResource(key);
    String file = Resources.toString(
        Preconditions.checkNotNull(resource, "Resource for %s could not be loaded.", key), Charsets.UTF_8);

    assertThat(writerMap.get(key).toString()).isEqualTo(file);
  }
}
