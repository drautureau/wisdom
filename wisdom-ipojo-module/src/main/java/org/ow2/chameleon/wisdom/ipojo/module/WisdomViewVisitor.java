package org.ow2.chameleon.wisdom.ipojo.module;

import org.apache.felix.ipojo.manipulator.Reporter;
import org.apache.felix.ipojo.manipulator.metadata.annotation.ComponentWorkbench;
import org.apache.felix.ipojo.metadata.Attribute;
import org.apache.felix.ipojo.metadata.Element;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.EmptyVisitor;
import org.objectweb.asm.tree.FieldNode;
import org.ow2.chameleon.wisdom.api.templates.Template;

/**
 * Visits the @View annotation and transform it into a @Requires with the right filter.
 */
public class WisdomViewVisitor extends EmptyVisitor implements AnnotationVisitor {
    private final Reporter reporter;
    private final ComponentWorkbench workbench;
    private final String field;
    private final FieldNode node;
    private String name;

    public WisdomViewVisitor(ComponentWorkbench workbench, Reporter reporter, FieldNode node) {
        this.reporter = reporter;
        this.workbench = workbench;
        this.node = node;
        this.field = node.name;
    }

    /**
     * Visits the @Template annotation value.
     * @param s value
     * @param o the value
     */
    @Override
    public void visit(String s, Object o) {
        this.name = o.toString();
    }

    @Override
    public void visitEnd() {
        if (name == null  || name.length() == 0) {
            reporter.error("The 'name' attribute of @View from " + workbench.getType().getClassName() + " must be " +
                    "set");
        }

        // Check the type of the field
        if (! Type.getDescriptor(Template.class).equals(node.desc)) {
            reporter.error("The type of the field " + field + " from " + workbench.getType().getClassName() + " must " +
                    "be " + Template.class.getName() + " because the field is annotated with @View");
        }

        Element requires = new Element("requires", "");
        requires.addAttribute(new Attribute("field", field));
        requires.addAttribute(new Attribute("filter", getFilter(name)));

        workbench.getElements().put(requires, null);
    }

    private String getFilter(String name) {
        return "(name=" + name + ")";
    }
}