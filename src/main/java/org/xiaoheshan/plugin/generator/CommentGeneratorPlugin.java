package org.xiaoheshan.plugin.generator;

import org.mybatis.generator.api.CommentGenerator;
import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.internal.util.StringUtility;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

/**
 * @author _Chf
 * @since 01-26-2018
 */
public class CommentGeneratorPlugin implements CommentGenerator {

    /** The properties. */
    private Properties properties;

    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");

    private static final String JAVA_DOC_PREFIX = "/**";
    private static final String JAVA_DOC_POSTFIX = "*/";

    private static final String LINE_SEPARATOR = "line.separator";

    public CommentGeneratorPlugin() {
        super();
        properties = new Properties();
    }

    @Override
    public void addConfigurationProperties(Properties properties) {
        this.properties.putAll(properties);

        String dateFormatString = properties.getProperty(PropertyRegistry.COMMENT_GENERATOR_DATE_FORMAT);
        if (StringUtility.stringHasValue(dateFormatString)) {
            dateFormat = new SimpleDateFormat(dateFormatString);
        }
    }

    @Override
    public void addFieldComment(Field field, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
        StringBuilder builder = new StringBuilder();
        builder.append(JAVA_DOC_PREFIX).append(' ');
        String remarks = introspectedColumn.getRemarks();
        if (StringUtility.stringHasValue(remarks)) {
            String[] remarkLines = remarks.split(System.getProperty(LINE_SEPARATOR));
            for (String remarkLine : remarkLines) {
                builder.append(remarkLine).append(' ');
            }
            builder.deleteCharAt(builder.length() - 1);
        }
        builder.append(' ').append(JAVA_DOC_POSTFIX);
        field.addJavaDocLine(builder.toString());
    }

    @Override
    public void addFieldComment(Field field, IntrospectedTable introspectedTable) {
        this.addFieldComment(field, introspectedTable, new IntrospectedColumn());
    }

    @Override
    public void addModelClassComment(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        topLevelClass.addAnnotation("@lombok.Data");
        topLevelClass.addJavaDocLine(JAVA_DOC_PREFIX);
        StringBuilder builder = new StringBuilder(" * ");
        String remarks = introspectedTable.getRemarks();
        if (StringUtility.stringHasValue(remarks)) {
            String[] remarkLines = remarks.split(System.getProperty(LINE_SEPARATOR));
            for (String remarkLine : remarkLines) {
                builder.append(remarkLine).append(' ');
            }
            builder.deleteCharAt(builder.length() - 1);
        } else {
            builder.append(introspectedTable.getFullyQualifiedTable());
        }
        builder.append("DO");
        topLevelClass.addJavaDocLine(builder.toString());
        topLevelClass.addJavaDocLine(" * ");
        topLevelClass.addJavaDocLine(" * @author " + System.getProperties().getProperty("user.name"));
        topLevelClass.addJavaDocLine(" * @since " + getDateString());
        topLevelClass.addJavaDocLine(' ' + JAVA_DOC_POSTFIX);
    }

    @Override
    public void addClassComment(InnerClass innerClass, IntrospectedTable introspectedTable) {
        //do nothing
    }

    @Override
    public void addClassComment(InnerClass innerClass, IntrospectedTable introspectedTable, boolean markAsDoNotDelete) {
        //do nothing
    }

    @Override
    public void addEnumComment(InnerEnum innerEnum, IntrospectedTable introspectedTable) {
        //do nothing
    }

    @Override
    public void addGetterComment(Method method, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
        //do nothing
    }

    @Override
    public void addSetterComment(Method method, IntrospectedTable introspectedTable, IntrospectedColumn introspectedColumn) {
        //do nothing
    }

    @Override
    public void addGeneralMethodComment(Method method, IntrospectedTable introspectedTable) {
        //do nothing
    }

    @Override
    public void addJavaFileComment(CompilationUnit compilationUnit) {
        // add no file level comments by default
    }

    @Override
    public void addComment(XmlElement xmlElement) {
        //do nothing
    }

    @Override
    public void addRootComment(XmlElement rootElement) {
        //do nothing
    }

    /**
     * This method returns a formated date string to include in the Javadoc tag
     * and XML comments. You may return null if you do not want the date in
     * these documentation elements.
     *
     * @return a string representing the current timestamp, or null
     */
    private String getDateString() {
        if (dateFormat != null) {
            return dateFormat.format(new Date());
        } else {
            return new Date().toString();
        }
    }
}
