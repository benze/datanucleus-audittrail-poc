package mydomain.datanucleus.datatrail.annotation;

import mydomain.audit.DataTrail;
import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.annotations.AnnotationObject;
import org.datanucleus.metadata.annotations.ClassAnnotationHandler;
import org.datanucleus.metadata.annotations.MemberAnnotationHandler;

public class DataTrailAnnotationHandler implements ClassAnnotationHandler, MemberAnnotationHandler {

    public static final String EXTENSION_CLASS_DATATRAIL_EXCLUDE = DataTrail.class.getName() + ".excludeFromDataTrail";
    public static final String EXTENSION_MEMBER_DATATRAIL_EXCLUDE = DataTrail.class.getName() + ".excludeFromDataTrail";

    @Override
    public void processClassAnnotation(final AnnotationObject annotation, final AbstractClassMetaData cmd, final ClassLoaderResolver clr) {
        final Boolean exclude =  (Boolean) annotation.getNameValueMap().getOrDefault("excludeFromDataTrail", false);
        cmd.addExtension(DataTrailAnnotationHandler.EXTENSION_CLASS_DATATRAIL_EXCLUDE, exclude.toString());
    }

    @Override
    public void processMemberAnnotation(final AnnotationObject annotation, final AbstractMemberMetaData mmd, final ClassLoaderResolver clr) {
        final Boolean exclude = (Boolean) annotation.getNameValueMap().getOrDefault("excludeFromDataTrail", false);
        mmd.addExtension(DataTrailAnnotationHandler.EXTENSION_MEMBER_DATATRAIL_EXCLUDE, exclude.toString());
    }
}
