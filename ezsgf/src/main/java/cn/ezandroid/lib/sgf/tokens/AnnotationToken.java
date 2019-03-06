package cn.ezandroid.lib.sgf.tokens;

/**
 * AnnotationToken
 *
 * @author like
 * @date 2019-01-09
 */
public class AnnotationToken extends TextToken implements InfoToken {

    public AnnotationToken() {}

    public String getAnnotation() {
        return getText();
    }
}

