package tests.dsl;

import io.github.sakurawald.fuji.core.command.argument.structure.CommandArgument;
import io.github.sakurawald.fuji.module.initializer.command_bundle.structure.BundleCommandDescriptor;
import io.github.sakurawald.fuji.module.initializer.command_bundle.structure.BundleCommandNode;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class BundleCommandModuleDSLTest {

    @Test
    void test1() {
        BundleCommandDescriptor descriptor = BundleCommandDescriptor.make(new BundleCommandNode(null, null, "my-command <int int-arg-name> [str str-arg-name]", null));
        System.out.println(descriptor);
        List<CommandArgument> args = descriptor.commandArguments;

        CommandArgument firstArg = args.get(0);
        assertTrue(firstArg.isLiteralArgument());
        assertEquals("my-command", firstArg.getArgumentName());
        assertFalse(firstArg.isOptional());

        CommandArgument secondArg = args.get(1);
        assertTrue(secondArg.isRequiredArgument());
        assertEquals("int-arg-name", secondArg.getArgumentName());
        assertFalse(secondArg.isOptional());

        CommandArgument thirdArg = args.get(2);
        assertTrue(thirdArg.isRequiredArgument());
        assertEquals("str-arg-name", thirdArg.getArgumentName());
        assertTrue(thirdArg.isOptional());
    }


    @Test
    void test2() {
        BundleCommandDescriptor descriptor = BundleCommandDescriptor.make(new BundleCommandNode(null, null, "my-command <int int-arg-name> first-literal [str str-arg-name] second-literal", null));
        System.out.println(descriptor);

        List<CommandArgument> args = descriptor.commandArguments;

        CommandArgument firstArg = args.get(0);
        assertTrue(firstArg.isLiteralArgument());
        assertEquals("my-command", firstArg.getArgumentName());
        assertFalse(firstArg.isOptional());

        CommandArgument secondArg = args.get(1);
        assertTrue(secondArg.isRequiredArgument());
        assertEquals("int-arg-name", secondArg.getArgumentName());
        assertFalse(secondArg.isOptional());

        CommandArgument thirdArg = args.get(2);
        assertTrue(thirdArg.isLiteralArgument());
        assertEquals("first-literal", thirdArg.getArgumentName());
        assertFalse(thirdArg.isOptional());

        CommandArgument fourthArg = args.get(3);
        assertTrue(fourthArg.isRequiredArgument());
        assertEquals("str-arg-name", fourthArg.getArgumentName());
        assertTrue(fourthArg.isOptional());

        CommandArgument fifth = args.get(4);
        assertTrue(fifth.isLiteralArgument());
        assertEquals("second-literal", fifth.getArgumentName());
        assertFalse(fifth.isOptional());
    }

    @Test
    void test3() {
        BundleCommandDescriptor descriptor = BundleCommandDescriptor.make(new BundleCommandNode(null, null, "my-command <int int-arg-name> [str str-arg-name hello world] first-literal", null));
        System.out.println(descriptor);

        List<CommandArgument> args = descriptor.commandArguments;

        CommandArgument firstArg = args.get(0);
        assertTrue(firstArg.isLiteralArgument());
        assertEquals("my-command", firstArg.getArgumentName());
        assertFalse(firstArg.isOptional());

        CommandArgument secondArg = args.get(1);
        assertTrue(secondArg.isRequiredArgument());
        assertEquals("int-arg-name", secondArg.getArgumentName());
        assertFalse(secondArg.isOptional());

        CommandArgument thirdArg = args.get(2);
        assertTrue(thirdArg.isRequiredArgument());
        assertEquals("str-arg-name", thirdArg.getArgumentName());
        assertTrue(thirdArg.isOptional());
        assertEquals("hello world", descriptor.getOptionalArgumentName2DefaultValue().get("str-arg-name"));

        CommandArgument fourthArg = args.get(3);
        assertTrue(fourthArg.isLiteralArgument());
        assertEquals("first-literal", fourthArg.getArgumentName());
        assertFalse(fourthArg.isOptional());

    }
}
