package tests.generator;

import auxiliary.TestUtil;
import auxiliary.classgraph.ClassGraphUtil;
import auxiliary.classgraph.structure.ExtendedAnnotationInfo;
import com.google.gson.JsonObject;
import io.github.classgraph.AnnotationClassRef;
import io.github.classgraph.AnnotationParameterValueList;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.MethodParameterInfo;
import io.github.classgraph.ScanResult;
import io.github.sakurawald.fuji.core.auxiliary.JsonUtil;
import io.github.sakurawald.fuji.core.auxiliary.ReflectionUtil;
import io.github.sakurawald.fuji.core.config.mapper.GsonMapper;
import io.github.sakurawald.fuji.core.event.annotation.EventConsumer;
import io.github.sakurawald.fuji.core.event.annotation.EventProducer;
import io.github.sakurawald.fuji.core.event.inject.structure.EventConsumerInfo;
import io.github.sakurawald.fuji.core.event.inject.structure.EventGraph;
import io.github.sakurawald.fuji.core.event.inject.structure.EventProducerInfo;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

public class GenerateEventGraphTest {

    @Test
    public void generateEventGraphFile() {
        try (ScanResult scanResult = ClassGraphUtil.makeScanResult()) {
            /* Generate event-graph.json file. */
            Path graphFilePath = TestUtil.COMPILE_TIME_GRAPH_PATH.resolve(ReflectionUtil.CompileTimeGraph.EVENT_GRAPH_FILE_NAME);

            EventGraph eventGraph = new EventGraph();

            collectEventProducers(scanResult, eventGraph);
            collectEventConsumers(scanResult, eventGraph);
            sortEventGraph(eventGraph);
            validateEventGraph(eventGraph);

            JsonObject eventGraphJsonObject = GsonMapper.toJsonTree(eventGraph).getAsJsonObject();
            JsonUtil.writeJsonObject(eventGraphJsonObject, graphFilePath);
        }
    }

    private void sortEventGraph(EventGraph eventGraph) {
        eventGraph
            .getConsumers()
            .sort(Comparator
                .comparing(EventConsumerInfo::getInjectorPriority)
                .thenComparing(EventConsumerInfo::getConsumerPriority));
    }

    private void validateEventGraph(EventGraph eventGraph) {
        eventGraph
            .getConsumers()
            .forEach(it -> {
                String eventTypeClassName = it.getEventTypeClassName();

                /* Ensure there are producers for required event type. */
                List<EventProducerInfo> eventProducerInfoList = eventGraph
                    .getProducers()
                    .stream()
                    .filter(eventProducerInfo -> eventProducerInfo.getEventTypeClassName().equals(eventTypeClassName))
                    .toList();

                if (eventProducerInfoList.isEmpty()) {
                    throw new IllegalStateException("There is no any producer to satisfy a consumer: eventType = %s".formatted(eventTypeClassName));
                }

                /* Ensure there are producers for required injector point. */
                int requiredInjectorPriority = it.getInjectorPriority();

                if (eventProducerInfoList
                    .stream()
                    .noneMatch(eventProducerInfo -> eventProducerInfo.getInjectorPriority() == requiredInjectorPriority)) {
                    throw new IllegalStateException("There is no any producer to satisfy a consumer: eventType = %s, injectorPriority = %s".formatted(eventTypeClassName, requiredInjectorPriority));
                }
            });

    }

    @SuppressWarnings("unused")
    private static void collectEventProducers(ScanResult scanResult, EventGraph eventGraph) {
        List<ExtendedAnnotationInfo> extendedAnnotationInfoList = ClassGraphUtil.findTargetAnnotationInstancesAnywhere(scanResult, EventProducer.class, false);
        extendedAnnotationInfoList.forEach(extendedAnnotationInfo -> {

            String declaringClassName = extendedAnnotationInfo.getDeclaringClass().getName();
            MethodInfo declaringMethod = extendedAnnotationInfo.getDeclaringMethod();
            String declaringMethodName = declaringMethod.getName();

            final String EVENT_PRODUCER_METHOD_PREFIX = "produce";
            if (!declaringMethodName.startsWith(EVENT_PRODUCER_METHOD_PREFIX)) {
                throw new IllegalStateException("The event producer method should start with %s. (class = %s, method = %s)".formatted(EVENT_PRODUCER_METHOD_PREFIX, declaringClassName, declaringMethodName));
            }

            EventProducerInfo eventProducerInfo = makeEventProducerInfo(extendedAnnotationInfo, declaringClassName, declaringMethodName);

            eventGraph
                .getProducers()
                .add(eventProducerInfo);
        });
    }

    private static @NotNull EventProducerInfo makeEventProducerInfo(ExtendedAnnotationInfo extendedAnnotationInfo, String declaringClassName, String declaringMethodName) {
        AnnotationParameterValueList parameterValues = extendedAnnotationInfo.getAnnotationInfo().getParameterValues();

        AnnotationClassRef annotationClassRef = (AnnotationClassRef) parameterValues.getValue("value");
        String eventTypeClassName = annotationClassRef.getClassInfo().getName();

        int injectorPriority = (int) parameterValues.getValue("injectorPriority");

        EventProducerInfo eventProducerInfo = new EventProducerInfo(eventTypeClassName, declaringClassName, declaringMethodName, injectorPriority);
        return eventProducerInfo;
    }

    private static void collectEventConsumers(ScanResult scanResult, EventGraph eventGraph) {
        List<ExtendedAnnotationInfo> extendedAnnotationInfoList = ClassGraphUtil.findTargetAnnotationInstancesAnywhere(scanResult, EventConsumer.class, false);
        extendedAnnotationInfoList.forEach(extendedAnnotationInfo -> {

            String declaringClassName = extendedAnnotationInfo.getDeclaringClass().getName();
            MethodInfo declaringMethod = extendedAnnotationInfo.getDeclaringMethod();
            String declaringMethodName = declaringMethod.getName();

            MethodParameterInfo[] parameterInfo = declaringMethod.getParameterInfo();
            if (parameterInfo.length != 1) {
                throw new IllegalArgumentException("Expecting exactly one parameter in method annotated with @EventHandler annotation.");
            }

            String resultType = declaringMethod.getTypeDescriptor().getResultType().toString();
            if (!resultType.equals("void")) {
                throw new IllegalArgumentException("The type of return value in method annotated with @EventHandler annotation must be 'void'.");
            }

            if (!declaringMethod.isStatic()) {
                throw new IllegalArgumentException("The method annotated with @EventHandler annotation must be 'static'.");
            }

            EventConsumerInfo eventConsumerInfo = makeEventConsumerInfo(extendedAnnotationInfo, parameterInfo[0].getTypeDescriptor().toString(), declaringClassName, declaringMethodName);
            eventGraph
                .getConsumers()
                .add(eventConsumerInfo);
        });
    }

    private static @NotNull EventConsumerInfo makeEventConsumerInfo(ExtendedAnnotationInfo extendedAnnotationInfo, String eventTypeClassName, String declaringClassName, String declaringMethodName) {
        int injectorPriority = (int) extendedAnnotationInfo.getAnnotationInfo().getParameterValues().getValue("injectorPriority");
        int consumerPriority = (int) extendedAnnotationInfo.getAnnotationInfo().getParameterValues().getValue("consumerPriority");

        return new EventConsumerInfo(eventTypeClassName, declaringClassName, declaringMethodName, injectorPriority, consumerPriority);
    }


}
