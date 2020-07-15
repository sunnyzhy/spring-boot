# Spring的自动装配模式
```java
public interface AutowireCapableBeanFactory extends BeanFactory {
	/**
	 * 无需自动装配，XML 配置中默认的自动装配模式为 no
	 */
	int AUTOWIRE_NO = 0;


	/**
	 * 按名称自动装配
	 */
	int AUTOWIRE_BY_NAME = 1;


	/**
	 * 按类型自动装配，Java 默认的自动装配模式是 byType
	 */
	int AUTOWIRE_BY_TYPE = 2;


	/**
	 * 按构造器自动装配
	 */
	int AUTOWIRE_CONSTRUCTOR = 3;


	/**
	 * @deprecated as of Spring 3.0
	 */
	@Deprecated
	int AUTOWIRE_AUTODETECT = 4;
}
```

# Spring支持的三种依赖注入的注解

- **@Autowired (Spring)**
- **@Inject (JSR-330)**
- **@Resource (JSR-250)**

# resolveDependency
resolveDependency支持的依赖注入：

- Optional (主要是将依赖设置非强制依赖，即 descriptor.required = false)
- 延迟注入 (ObjectFactory、ObjectProvider、javax.inject.Provider)
- 懒加载注入 (另一种延迟注入 @Lazy )
- 正常注入

```java
@Override
@Nullable
public Object resolveDependency(DependencyDescriptor descriptor, @Nullable String requestingBeanName,
    @Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) throws BeansException {

  // 初始化方法的参数名称
  descriptor.initParameterNameDiscovery(getParameterNameDiscoverer());
  // 1. Optional<T>
  if (Optional.class == descriptor.getDependencyType()) {
    return createOptionalDependency(descriptor, requestingBeanName);
  }
  // 2. ObjectFactory<T>、ObjectProvider<T>
  else if (ObjectFactory.class == descriptor.getDependencyType() ||
      ObjectProvider.class == descriptor.getDependencyType()) {
    return new DependencyObjectProvider(descriptor, requestingBeanName);
  }
  // 3. javax.inject.Provider<T>
  else if (javaxInjectProviderClass == descriptor.getDependencyType()) {
    return new Jsr330Factory().createDependencyProvider(descriptor, requestingBeanName);
  }
  else {
    // 4. @Lazy
    Object result = getAutowireCandidateResolver().getLazyResolutionProxyIfNecessary(
        descriptor, requestingBeanName);
    // 5. 正常注入
    if (result == null) {
      result = doResolveDependency(descriptor, requestingBeanName, autowiredBeanNames, typeConverter);
    }
    return result;
  }
}
```

# doResolveDependency
```java
@Nullable
public Object doResolveDependency(DependencyDescriptor descriptor, @Nullable String beanName,
    @Nullable Set<String> autowiredBeanNames, @Nullable TypeConverter typeConverter) throws BeansException {

  InjectionPoint previousInjectionPoint = ConstructorResolver.setCurrentInjectionPoint(descriptor);
  try {
    Object shortcut = descriptor.resolveShortcut(this);
    if (shortcut != null) {
      return shortcut;
    }

    // 1. @Value 注解处理场景，String 要经过三个过程：1. 占位符处理 -> 2. EL 表达式解析 -> 3. 类型转换
    Class<?> type = descriptor.getDependencyType();
    Object value = getAutowireCandidateResolver().getSuggestedValue(descriptor);
    if (value != null) {
      if (value instanceof String) {
        // 1.1 占位符解析
        String strVal = resolveEmbeddedValue((String) value);
        BeanDefinition bd = (beanName != null && containsBean(beanName) ?
            getMergedBeanDefinition(beanName) : null);
        // 1.2 Spring EL 表达式
        value = evaluateBeanDefinitionString(strVal, bd);
      }
      // 1.3 类型转换
      TypeConverter converter = (typeConverter != null ? typeConverter : getTypeConverter());
      try {
        return converter.convertIfNecessary(value, type, descriptor.getTypeDescriptor());
      }
      catch (UnsupportedOperationException ex) {
        // A custom TypeConverter which does not support TypeDescriptor resolution...
        return (descriptor.getField() != null ?
            converter.convertIfNecessary(value, type, descriptor.getField()) :
            converter.convertIfNecessary(value, type, descriptor.getMethodParameter()));
      }
    }

    // 集合依赖，如 Array、List、Set、Map
    Object multipleBeans = resolveMultipleBeans(descriptor, beanName, autowiredBeanNames, typeConverter);
    if (multipleBeans != null) {
      return multipleBeans;
    }

    // 2. 查找依赖
    Map<String, Object> matchingBeans = findAutowireCandidates(beanName, type, descriptor);
    // 2.1 没有查找到依赖，判断descriptor.require
    if (matchingBeans.isEmpty()) {
      if (isRequired(descriptor)) {
        raiseNoMatchingBeanFound(type, descriptor.getResolvableType(), descriptor);
      }
      return null;
    }

    String autowiredBeanName;
    Object instanceCandidate;

    // 2.2 查找到多个依赖
    if (matchingBeans.size() > 1) {
      // 2.2.1 根据规则匹配：@Primary -> @Priority -> 方法名称或字段名称
      autowiredBeanName = determineAutowireCandidate(matchingBeans, descriptor);
      // 2.2.2 匹配到的依赖为空。注意这里如果是集合处理，则返回null
      if (autowiredBeanName == null) {
        // 如果满足条件，就抛出异常 NoUniqueBeanDefinitionException
        if (isRequired(descriptor) || !indicatesMultipleBeans(type)) {
          return descriptor.resolveNotUnique(descriptor.getResolvableType(), matchingBeans);
        }
        else {
          // In case of an optional Collection/Map, silently ignore a non-unique case:
          // possibly it was meant to be an empty collection of multiple regular beans
          // (before 4.3 in particular when we didn't even look for collection beans).
          return null;
        }
      }
      instanceCandidate = matchingBeans.get(autowiredBeanName);
    }
    // 2.3 查找到一个依赖
    else {
      // We have exactly one match.
      Map.Entry<String, Object> entry = matchingBeans.entrySet().iterator().next();
      autowiredBeanName = entry.getKey();
      instanceCandidate = entry.getValue();
    }

    // 2.4 处理命中的唯一依赖
    if (autowiredBeanNames != null) {
      autowiredBeanNames.add(autowiredBeanName);
    }
    // 2.5 descriptor.resolveCandidate 方法根据名称 autowiredBeanName 实例化对象
    if (instanceCandidate instanceof Class) {
      instanceCandidate = descriptor.resolveCandidate(autowiredBeanName, type, this);
    }
    Object result = instanceCandidate;
    if (result instanceof NullBean) {
      if (isRequired(descriptor)) {
        raiseNoMatchingBeanFound(type, descriptor.getResolvableType(), descriptor);
      }
      result = null;
    }
    if (!ClassUtils.isAssignableValue(type, result)) {
      throw new BeanNotOfRequiredTypeException(autowiredBeanName, type, instanceCandidate.getClass());
    }
    return result;
  }
  finally {
    ConstructorResolver.setCurrentInjectionPoint(previousInjectionPoint);
  }
}
```

matchingBeans 中的 Object 可能是对象类型，也可能是对象实例。因为 findAutowireCandidates 方法是根据类型 type 查找名称 beanNames，如果容器中该 beanName 还没有实例化，findAutowireCandidates 不会直接实例化该 bean，当然如果已经实例化了会直接返回这个 bean。

# findAutowireCandidates

无论是集合依赖还是单一依赖查找，本质上都是调用 findAutowireCandidates 进行类型依赖查找。

```java
protected Map<String, Object> findAutowireCandidates(
    @Nullable String beanName, Class<?> requiredType, DependencyDescriptor descriptor) {

  // 1. 类型查找：本质上递归调用 beanFactory#beanNamesForType。先匹配实例类型，再匹配BeanDefinition。
  String[] candidateNames = BeanFactoryUtils.beanNamesForTypeIncludingAncestors(
      this, requiredType, true, descriptor.isEager());
  Map<String, Object> result = new LinkedHashMap<>(candidateNames.length);
  // 2. Spring IoC 内部依赖 resolvableDependencies
  for (Map.Entry<Class<?>, Object> classObjectEntry : this.resolvableDependencies.entrySet()) {
    Class<?> autowiringType = classObjectEntry.getKey();
    if (autowiringType.isAssignableFrom(requiredType)) {
      Object autowiringValue = classObjectEntry.getValue();
      autowiringValue = AutowireUtils.resolveAutowiringValue(autowiringValue, requiredType);
      if (requiredType.isInstance(autowiringValue)) {
        result.put(ObjectUtils.identityToString(autowiringValue), autowiringValue);
        break;
      }
    }
  }
  for (String candidate : candidateNames) {
    // 1.1 isSelfReference 说明 beanName 和 candidate 本质是同一个对象
    //     isAutowireCandidate 进一步匹配 beanName.autowireCandidate、泛型、@@Qualifier 等进行过滤
    if (!isSelfReference(beanName, candidate) && isAutowireCandidate(candidate, descriptor)) {
      // 1.2 添加到候选对象中
      addCandidateEntry(result, candidate, descriptor, requiredType);
    }
  }
  // 3. 补偿机制：如果依赖查找无法匹配，就使用泛型补偿和自身引用补偿。
  if (result.isEmpty()) {
    boolean multiple = indicatesMultipleBeans(requiredType);
    // Consider fallback matches if the first pass failed to find anything...
    // 3.1 fallbackDescriptor: 泛型补偿，实际上是允许注入对象类型的泛型存在无法解析的情况
    DependencyDescriptor fallbackDescriptor = descriptor.forFallbackMatch();
    // 3.2 补偿1：不允许自称依赖，但如果是集合依赖，需要过滤非@Qualifier对象。
    for (String candidate : candidateNames) {
      if (!isSelfReference(beanName, candidate) && isAutowireCandidate(candidate, fallbackDescriptor) &&
          (!multiple || getAutowireCandidateResolver().hasQualifier(descriptor))) {
        addCandidateEntry(result, candidate, descriptor, requiredType);
      }
    }
    // 3.3 补偿2：允许自称依赖，但如果是集合依赖，注入的集合依赖中需要过滤自己
    if (result.isEmpty() && !multiple) {
      // Consider self references as a final pass...
      // but in the case of a dependency collection, not the very same bean itself.
      for (String candidate : candidateNames) {
        if (isSelfReference(beanName, candidate) &&
            (!(descriptor instanceof MultiElementDescriptor) || !beanName.equals(candidate)) &&
            isAutowireCandidate(candidate, fallbackDescriptor)) {
          addCandidateEntry(result, candidate, descriptor, requiredType);
        }
      }
    }
  }
  return result;
}
```

findAutowireCandidates 大致可以分为三步：先查找内部依赖，再根据类型查找，最后没有可注入的依赖则进行补偿。

1. 查找内部依赖：Spring IoC 容器本身相关依赖，这部分内容是用户而言是透明的，也不用感知。resolvableDependencies 集合中注册如 BeanFactory、ApplicationContext 、ResourceLoader、ApplicationEventPublisher 等。

2. 根据类型查找：包括 "外部托管 Bean" 和"注册 BeanDefinition"。类型查找调用 beanFactory#beanNamesForType 方法。
   - 自身引用：isSelfReference 方法判断 beanName 和 candidate 是否是同一个对象，包括两种情况：一是名称完全相同，二是 candidate 对应的工厂对象创建了 beanName。
   - 是否可以注入：底层实际调用 resolver.isAutowireCandidate 方法进行过滤，包含三重规则：1. BeanDefinition.autowireCandidate=true -> 2. 泛型匹配 -> 3. @Qualifier。

3. 补偿机制：如果依赖查找无法匹配，Spring 提供了两种补偿机制：一是泛型补偿，允许注入对象对象的泛型无法解析，二是自身引用补偿，对这两种机制使用如下：
   - 先使用泛型补偿，不允许自身引用：即 fallbackDescriptor。此时如果是集合依赖，对象必须是 @Qualifier 类型。
   - 允许泛型补偿和自身引用补偿：但如果是集合依赖，必须过滤自己本身，即 beanName.equals(candidate) 必须剔除。

# addCandidateEntry

```java
private void addCandidateEntry(Map<String, Object> candidates, String candidateName,
    DependencyDescriptor descriptor, Class<?> requiredType) {

  // 1. 集合依赖，直接调用 getName(candidateName) 实例化
  if (descriptor instanceof MultiElementDescriptor) {
    Object beanInstance = descriptor.resolveCandidate(candidateName, requiredType, this);
    if (!(beanInstance instanceof NullBean)) {
      candidates.put(candidateName, beanInstance);
    }
  }
  // 2. 已经实例化，直接返回实例对象
  else if (containsSingleton(candidateName) || (descriptor instanceof StreamDependencyDescriptor &&
      ((StreamDependencyDescriptor) descriptor).isOrdered())) {
    Object beanInstance = descriptor.resolveCandidate(candidateName, requiredType, this);
    candidates.put(candidateName, (beanInstance instanceof NullBean ? null : beanInstance));
  }
  // 3. 只获取candidateName的类型，真正需要注入时才实例化对象
  else {
    candidates.put(candidateName, getType(candidateName));
  }
}
```

descriptor.resolveCandidate 基本上都是直接调用 getName(beanName) 实例化 bean。在大部分场景中，addCandidateEntry 方法只会以返回该 candidateName 对应的类型，而不会提前实例该对象。

# isAutowireCandidate
isAutowireCandidate 方法过滤候选对象有三重规则：1. BeanDefinition.autowireCandidate=true -> 2. 泛型匹配 -> 3. @Qualifier。

```java
	protected boolean isAutowireCandidate(String beanName, RootBeanDefinition mbd,
			DependencyDescriptor descriptor, AutowireCandidateResolver resolver) {

		String bdName = BeanFactoryUtils.transformedBeanName(beanName);
    // 1. 传统方式：解析 BeanDefinition.beanClass，注意 Spring注解驱动时根本不会配置beanClassName
		resolveBeanClass(mbd, bdName);
    // 2. 注解驱动：解析工厂方法 BeanDefinition.factoryMethodToIntrospect
		if (mbd.isFactoryMethodUnique && mbd.factoryMethodToIntrospect == null) {
			new ConstructorResolver(this).resolveFactoryMethodIfPossible(mbd);
		}
    // 3. 直接委托给AutowireCandidateResolver
		BeanDefinitionHolder holder = (beanName.equals(bdName) ?
				this.mergedBeanDefinitionHolders.computeIfAbsent(beanName,
						key -> new BeanDefinitionHolder(mbd, beanName, getAliases(bdName))) :
				new BeanDefinitionHolder(mbd, beanName, getAliases(bdName)));
		return resolver.isAutowireCandidate(holder, descriptor);
	}
```

传统方式和注解驱动获取 Bean 类型的不同：

- 传统方式：配置 beanClassName，直接解析成 beanClass，从而获取对象类型。
- 注解驱动：如 @Bean 方式，需要解析方法返回值类型，获取对象类型。

