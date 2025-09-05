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
import io.github.sakurawald.fuji.core.event.inject.structure.EventConsumerInfoList;
import io.github.sakurawald.fuji.core.event.inject.structure.EventGraph;
import io.github.sakurawald.fuji.core.event.inject.structure.EventProducerInfo;
import io.github.sakurawald.fuji.core.event.inject.structure.EventProducerInfoList;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
            .values()
            .forEach(consumer -> consumer.sort(
                Comparator
                    .comparing(EventConsumerInfo::getInjectorPriority)
                    .thenComparing(EventConsumerInfo::getConsumerPriority)));
    }

    private void validateEventGraph(EventGraph eventGraph) {

        eventGraph
            .getConsumers()
            .entrySet()
            .stream()
            .forEach(it -> {
                String eventTypeName = it.getKey();
                EventConsumerInfoList eventConsumers = it.getValue();

                /* Ensure there are producers for required event type. */
                EventProducerInfoList eventProducerInfos = Optional
                    .ofNullable(eventGraph.getProducers().get(eventTypeName))
                    .orElseThrow(() -> new IllegalStateException("There is no any producer to satisfy a consumer: eventType = %s".formatted(eventTypeName)));

                /* Ensure there are producers for required injector point. */
                eventConsumers.forEach(eventConsumerInfo -> {
                    int requiredInjectorPriority = eventConsumerInfo.getInjectorPriority();

                    if (eventProducerInfos
                        .stream()
                        .noneMatch(eventProducerInfo -> eventProducerInfo.getInjectorPriority() == requiredInjectorPriority)) {
                        throw new IllegalStateException("There is no any producer to satisfy a consumer: eventType = %s, injectorPriority = %s".formatted(eventTypeName, requiredInjectorPriority));
                    }
                });
            });

    }

    @SuppressWarnings("unused")
    private static void collectEventProducers(ScanResult scanResult, EventGraph eventGraph) {
        List<ExtendedAnnotationInfo> extendedAnnotationInfoList = ClassGraphUtil.findTargetAnnotationInstancesAnywhere(scanResult, EventProducer.class, false);
        extendedAnnotationInfoList.forEach(extendedAnnotationInfo -> {

            String declaringClassName = extendedAnnotationInfo.getDeclaringClass().getName();
            MethodInfo declaringMethod = extendedAnnotationInfo.getDeclaringMethod();
            String declaringMethodName = declaringMethod.getName();

            AnnotationParameterValueList parameterValues = extendedAnnotationInfo.getAnnotationInfo().getParameterValues();

            AnnotationClassRef annotationClassRef = (AnnotationClassRef) parameterValues.getValue("value");
            String eventName = annotationClassRef.getClassInfo().getName();

            int injectorPriority = (int) parameterValues.getValue("injectorPriority");

            EventProducerInfo eventProducerInfo = new EventProducerInfo(declaringClassName, declaringMethodName, injectorPriority);

            eventGraph
                .getProducers()
                .computeIfAbsent(eventName, k -> new EventProducerInfoList())
                .add(eventProducerInfo);
        });
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

            String eventName = parameterInfo[0].getTypeDescriptor().toString();
            if (!eventGraph.getProducers().containsKey(eventName)) {
                throw new IllegalArgumentException("There is no event producer for the event type: " + eventName);
            }

            int injectorPriority = (int) extendedAnnotationInfo.getAnnotationInfo().getParameterValues().getValue("injectorPriority");
            int consumerPriority = (int) extendedAnnotationInfo.getAnnotationInfo().getParameterValues().getValue("consumerPriority");

            EventConsumerInfo eventConsumerInfo = new EventConsumerInfo(declaringClassName, declaringMethodName, injectorPriority, consumerPriority);
            eventGraph
                .getConsumers()
                .computeIfAbsent(eventName, k -> new EventConsumerInfoList())
                .add(eventConsumerInfo);
        });
    }


}
