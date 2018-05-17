package org.xiaoheshan.plugin.generator;

import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.Document;
import org.mybatis.generator.api.dom.xml.TextElement;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.codegen.XmlConstants;
import org.mybatis.generator.config.PropertyRegistry;
import org.mybatis.generator.internal.util.StringUtility;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author _Chf
 * @since 01-26-2018
 */
public class ExtGeneratorPlugin extends PluginAdapter {

    private static final String JAVA_DOC_PREFIX = "/**";
    private static final String JAVA_DOC_POSTFIX = "*/";
    private static final String LINE_SEPARATOR = "line.separator";
    private static String XML_FILE_POSTFIX = "Ext";
    private static String JAVA_FILE_POSTFIX = "Ext";
    private static String ANNOTATION_RESOURCE = "org.apache.ibatis.annotations.Mapper";
    private static String MAPPER_EXT_HINT = "<!-- 扩展自动生成或自定义的SQl语句写在此文件中 -->";

    private SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");

    /**
     * 添删改Document的sql语句及属性
     *
     * @param document
     * @param introspectedTable
     * @return
     */
    @Override
    public boolean sqlMapDocumentGenerated(Document document, IntrospectedTable introspectedTable) {
        XmlElement parentElement = document.getRootElement();
        updateDocumentNameSpace(introspectedTable, parentElement);
        generateMysqlPageSql(parentElement, introspectedTable);
        return super.sqlMapDocumentGenerated(document, introspectedTable);
    }

    /**
     * 生成XXExt.xml
     *
     * @param introspectedTable
     * @return
     */
    @Override
    public List<GeneratedXmlFile> contextGenerateAdditionalXmlFiles(IntrospectedTable introspectedTable) {
        String[] splitFile = introspectedTable.getMyBatis3XmlMapperFileName().split("\\.");
        String fileNameExt = null;
        if (splitFile[0] != null) {
            fileNameExt = splitFile[0] + XML_FILE_POSTFIX + ".xml";
        }

        if (isExistExtFile(context.getSqlMapGeneratorConfiguration().getTargetProject(),
                introspectedTable.getMyBatis3XmlMapperPackage(),
                fileNameExt)) {
            return super.contextGenerateAdditionalXmlFiles(introspectedTable);
        }

        Document document = new Document(XmlConstants.MYBATIS3_MAPPER_PUBLIC_ID, XmlConstants.MYBATIS3_MAPPER_SYSTEM_ID);

        XmlElement root = new XmlElement("mapper");
        document.setRootElement(root);
        String namespace = introspectedTable.getMyBatis3SqlMapNamespace() + XML_FILE_POSTFIX;
        root.addAttribute(new Attribute("namespace", namespace));
        root.addElement(new TextElement(MAPPER_EXT_HINT));

        GeneratedXmlFile gxf = new GeneratedXmlFile(
                document,
                fileNameExt,
                introspectedTable.getMyBatis3XmlMapperPackage(),
                context.getSqlMapGeneratorConfiguration().getTargetProject(),
                true,
                context.getXmlFormatter());

        List<GeneratedXmlFile> answer = new ArrayList<GeneratedXmlFile>(1);
        answer.add(gxf);

        return answer;
    }

    /**
     * 生成XXExt.java
     *
     * @param introspectedTable
     * @return
     */
    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
        /* 生成基本接口 */
        FullyQualifiedJavaType type = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType() + JAVA_FILE_POSTFIX);
        Interface interfaze = new Interface(type);
        interfaze.setVisibility(JavaVisibility.PUBLIC);
        context.getCommentGenerator().addJavaFileComment(interfaze);

        /* 添加父接口 */
        FullyQualifiedJavaType baseInterfaze = new FullyQualifiedJavaType(introspectedTable.getMyBatis3JavaMapperType());
        interfaze.addSuperInterface(baseInterfaze);

        /* 添加注解 */
        FullyQualifiedJavaType annotation = new FullyQualifiedJavaType(ANNOTATION_RESOURCE);
        interfaze.addAnnotation("@Mapper");
        interfaze.addImportedType(annotation);

        /* 添加注释 */
        addJavaDoc(interfaze, introspectedTable);

        /* 生成文件 */
        GeneratedJavaFile generatedJavaFile = new GeneratedJavaFile(interfaze,
                context.getJavaModelGeneratorConfiguration().getTargetProject(),
                context.getProperty(PropertyRegistry.CONTEXT_JAVA_FILE_ENCODING),
                context.getJavaFormatter());

        if (isExistExtFile(generatedJavaFile.getTargetProject(),
                generatedJavaFile.getTargetPackage(),
                generatedJavaFile.getFileName())) {
            return super.contextGenerateAdditionalJavaFiles(introspectedTable);
        }
        List<GeneratedJavaFile> generatedJavaFiles = new ArrayList<GeneratedJavaFile>(1);
        generatedJavaFile.getFileName();
        generatedJavaFiles.add(generatedJavaFile);
        return generatedJavaFiles;
    }

    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    @Override
    public boolean modelGetterMethodGenerated(Method method,
                                              TopLevelClass topLevelClass,
                                              IntrospectedColumn introspectedColumn,
                                              IntrospectedTable introspectedTable,
                                              ModelClassType modelClassType) {
        return false;
    }

    @Override
    public boolean modelSetterMethodGenerated(Method method,
                                              TopLevelClass topLevelClass,
                                              IntrospectedColumn introspectedColumn,
                                              IntrospectedTable introspectedTable,
                                              ModelClassType modelClassType) {
        return false;
    }

    /**
     * 添加javadoc
     *
     * @param javaElement
     * @param introspectedTable
     */
    private void addJavaDoc(JavaElement javaElement, IntrospectedTable introspectedTable) {
        javaElement.addJavaDocLine(JAVA_DOC_PREFIX);
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
        builder.append("Mapper");
        javaElement.addJavaDocLine(builder.toString());
        javaElement.addJavaDocLine(" * ");
        javaElement.addJavaDocLine(" * @author " + System.getProperties().getProperty("user.name"));
        javaElement.addJavaDocLine(" * @since " + getDateString());
        javaElement.addJavaDocLine(' ' + JAVA_DOC_POSTFIX);
    }

    private void generateMysqlPageSql(XmlElement parentElement, IntrospectedTable introspectedTable) {
        // mysql分页语句前半部分
        String tableName = introspectedTable.getTableConfiguration().getTableName();
        XmlElement paginationPrefixElement = new XmlElement("sql");
        context.getCommentGenerator().addComment(paginationPrefixElement);
        paginationPrefixElement.addAttribute(new Attribute("id", "MysqlDialectPrefix"));
        XmlElement pageStart = new XmlElement("if");
        pageStart.addAttribute(new Attribute("test", "page != null"));
        pageStart.addElement(new TextElement("from " + tableName + " where id in ( select id from ( select id "));
        paginationPrefixElement.addElement(pageStart);
        parentElement.addElement(paginationPrefixElement);

        // mysql分页语句后半部分
        XmlElement paginationSuffixElement = new XmlElement("sql");
        context.getCommentGenerator().addComment(paginationSuffixElement);
        paginationSuffixElement.addAttribute(new Attribute("id", "MysqlDialectSuffix"));
        XmlElement pageEnd = new XmlElement("if");
        pageEnd.addAttribute(new Attribute("test", "page != null"));
        pageEnd.addElement(new TextElement("<![CDATA[ limit #{page.begin}, #{page.length} ) as temp_page_table) ]]>"));
        XmlElement orderByElement = new XmlElement("if");
        orderByElement.addAttribute(new Attribute("test", "orderByClause != null"));
        orderByElement.addElement(new TextElement("order by ${orderByClause}"));
        pageEnd.addElement(orderByElement);
        paginationSuffixElement.addElement(pageEnd);

        parentElement.addElement(paginationSuffixElement);
    }

    private void updateDocumentNameSpace(IntrospectedTable introspectedTable, XmlElement parentElement) {
        Attribute namespaceAttribute = null;
        for (Attribute attribute : parentElement.getAttributes()) {
            if ("namespace".equals(attribute.getName())) {
                namespaceAttribute = attribute;
            }
        }
        parentElement.getAttributes().remove(namespaceAttribute);
        parentElement.getAttributes().add(new Attribute("namespace", introspectedTable.getMyBatis3JavaMapperType() + JAVA_FILE_POSTFIX));
    }

    private boolean isExistExtFile(String targetProject, String targetPackage, String fileName) {

        File project = new File(targetProject);
        if (!project.isDirectory()) {
            return true;
        }

        StringBuilder sb = new StringBuilder();
        StringTokenizer st = new StringTokenizer(targetPackage, ".");
        while (st.hasMoreTokens()) {
            sb.append(st.nextToken());
            sb.append(File.separatorChar);
        }

        File directory = new File(project, sb.toString());
        if (!directory.isDirectory()) {
            boolean rc = directory.mkdirs();
            if (!rc) {
                return true;
            }
        }

        File testFile = new File(directory, fileName);

        return testFile.exists();
    }

    private String getDateString() {
        if (dateFormat != null) {
            return dateFormat.format(new Date());
        } else {
            return new Date().toString();
        }
    }
}
