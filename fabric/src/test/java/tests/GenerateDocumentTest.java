package tests;

import auxiliary.TestUtility;
import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.AnnotationParameterValueList;
import io.github.classgraph.ScanResult;
import io.github.sakurawald.fuji.core.auxiliary.LogUtil;
import io.github.sakurawald.fuji.core.document.annotation.Document;
import org.junit.jupiter.api.Test;

public class GenerateDocumentTest {

    @Test
    void generateDocumentAnnotationInRuntimeEnvironment() {
        try (ScanResult scanResult = TestUtility.makeBaseClassGraph()
            .enableAllInfo()
            .scan()) {

            scanResult
                .getClassesWithAnnotation(Document.class)
                .forEach(classInfo -> {
                    AnnotationInfo annotationInfo = classInfo.getAnnotationInfo(Document.class);
                    AnnotationParameterValueList parameterValues = annotationInfo.getParameterValues();
                    LogUtil.info("parameter values = {}", parameterValues);

                });

        }
    }

    void setDocument(AnnotationInfo annotationInfo) {
        AnnotationParameterValueList parameterValues = annotationInfo.getParameterValues();
        Long id = (Long) parameterValues.getValue("id");
        String documentString = (String) parameterValues.getValue("value");

    }

}
