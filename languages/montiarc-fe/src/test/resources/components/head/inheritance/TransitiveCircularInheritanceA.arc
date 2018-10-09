package components.head.inheritance;

/**
 * Invalid model.
 * Circular inheritance is not allowed (see TransitiveCircularInheritanceC.arc)

 * @implements [Hab16] R11: Inheritance cycles of component types are
 *  forbidden. (p. 67, lst. 3.46)
 */
component TransitiveCircularInheritanceA extends TransitiveCircularInheritanceB {
  // Empty body
}
