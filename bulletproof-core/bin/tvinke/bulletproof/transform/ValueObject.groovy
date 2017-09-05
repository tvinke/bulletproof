package tvinke.bulletproof.transform

import groovy.transform.AnnotationCollector
import groovy.transform.Immutable

/**
 * The {@code ValueObject} meta-annotation combines the {@code Immutable} and
 * {@code NonNull} annotations. 
 * 
 * <p>It's used to assist in the creation of <a href="https://en.wikipedia.org/wiki/Value_object">value objects</a>. 
 * 
 * <p>Characteristics are that these objects are immutable, which is taken care of by 
 * the {@code Immutable} annotation, and the {@code NonNull} annotation additionally makes 
 * sure you can not construct an object with {@code null} values.
 * 
 * @author Ted Vinke
 *
 * @see tvinke.bulletproof.transform.NonNull
 * @see groovy.transform.Immutable
 */
@AnnotationCollector(value=[Immutable, NonNull])
@interface ValueObject { }
