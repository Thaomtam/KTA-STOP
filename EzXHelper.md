EzXHelper/com.github.kyuubiran.ezxhelper.utils
Package-level declarations

Functions
addModuleAssetPath
fun Context.addModuleAssetPath()
扩展函数 将模块的资源路径添加到Context.resources内 允许直接以R.xx.xxx获取资源

fun Resources.addModuleAssetPath()
扩展函数 将模块的资源路径添加到resources内 允许直接以R.xx.xxx获取资源

applyRemoveIf
inline fun <K, V> MutableMap<K, V>.applyRemoveIf(
    predicate: (K, V) -> Boolean
): MutableMap<K, V>
扩展函数 移除可变字典中符合条件的元素 并返回可变字典

applyRetainIf
inline fun <E> MutableList<E>.applyRetainIf(predicate: (E) -> Boolean): MutableList<E>
扩展函数 保留可变列表中符合条件的元素 并返回可变列表

inline fun <K, V> MutableMap<K, V>.applyRetainIf(
    predicate: (K, V) -> Boolean
): MutableMap<K, V>
扩展函数 保留可变字典中符合条件的元素 并返回可变字典

inline fun <E> MutableSet<E>.applyRetainIf(predicate: (E) -> Boolean): MutableSet<E>
扩展函数 保留可变集合中符合条件的元素 并返回可变集合

args
inline fun args(vararg args: Any?): Args
argTypes
inline fun argTypes(vararg argTypes: Class<*>): ArgTypes
buildJSONArray
inline fun buildJSONArray(builder: JSONArray.() -> Unit): JSONArray
构建一个JSONArray

buildJSONObject
inline fun buildJSONObject(builder: JSONObject.() -> Unit): JSONObject
构建一个JSONObject

field
fun Any.field(
    fieldName: String, 
    isStatic: Boolean = false, 
    fieldType: Class<*>? = null
): Field
扩展函数 通过类或者对象获取单个属性

fieldCpy
fun <T> fieldCpy(srcObj: T, newObj: T): T?
深拷贝一个对象

filter
inline fun JSONArray.filter(predicate: (Any) -> Boolean): JSONArray
扩展函数 对JSONArray进行过滤 并返回新的JSONArray

findAllConstructors
inline fun Class<*>.findAllConstructors(
    noinline condition: ConstructorCondition
): List<Constructor<*>>

fun findAllConstructors(clz: Class<*>, condition: ConstructorCondition): List<Constructor<*>>
fun findAllConstructors(
    clzName: String, 
    classLoader: ClassLoader = InitFields.ezXClassLoader, 
    condition: ConstructorCondition
): List<Constructor<*>>
查找所有符合条件的构造方法

findAllFields
fun Array<Field>.findAllFields(condition: FieldCondition): Array<Field>
扩展函数 通过遍历属性数组 返回符合条件的属性数组

fun Iterable<Field>.findAllFields(condition: FieldCondition): List<Field>
inline fun Class<*>.findAllFields(
    findSuper: Boolean = false, 
    noinline condition: FieldCondition
): List<Field>

fun findAllFields(
    clz: Class<*>, 
    findSuper: Boolean = false, 
    condition: FieldCondition
): List<Field>
fun findAllFields(
    clzName: String, 
    classLoader: ClassLoader = InitFields.ezXClassLoader, 
    findSuper: Boolean = false, 
    condition: FieldCondition
): List<Field>
通过条件获取属性数组

findAllMethods
fun Array<Method>.findAllMethods(condition: MethodCondition): Array<Method>
扩展函数 通过遍历方法数组 返回符合条件的方法数组

fun Array<Pair<Class<*>, MethodCondition>>.findAllMethods(
    findSuper: Boolean = false
): Array<Method>
扩展函数 加载数组中的类并且通过条件查找方法

fun Iterable<Method>.findAllMethods(condition: MethodCondition): List<Method>
fun Iterable<Pair<Class<*>, MethodCondition>>.findAllMethods(
    findSuper: Boolean = false
): List<Method>
inline fun Class<*>.findAllMethods(
    findSuper: Boolean = false, 
    noinline condition: MethodCondition
): List<Method>
fun Iterable<Class<*>>.findAllMethods(
    findSuper: Boolean = false, 
    condition: MethodCondition
): List<Method>

fun Array<Class<*>>.findAllMethods(
    findSuper: Boolean = false, 
    condition: MethodCondition
): Array<Method>
扩展函数 通过条件搜索所有方法

fun findAllMethods(
    clz: Class<*>, 
    findSuper: Boolean = false, 
    condition: MethodCondition
): List<Method>
fun findAllMethods(
    clzName: String, 
    classLoader: ClassLoader = InitFields.ezXClassLoader, 
    findSuper: Boolean = false, 
    condition: MethodCondition
): List<Method>
通过条件获取方法数组

findAllViewsByCondition
fun ViewGroup.findAllViewsByCondition(condition: (view: View) -> Boolean): List<View>
扩展函数 遍历ViewGroup 根据条件查找所有符合条件的View

findConstructor
inline fun Class<*>.findConstructor(noinline condition: ConstructorCondition): Constructor<*>
fun Iterable<Constructor<*>>.findConstructor(condition: ConstructorCondition): Constructor<*>

fun Array<Constructor<*>>.findConstructor(condition: ConstructorCondition): Constructor<*>
扩展函数 通过条件查找构造方法

fun findConstructor(clz: Class<*>, condition: ConstructorCondition): Constructor<*>
fun findConstructor(
    clzName: String, 
    classLoader: ClassLoader = InitFields.ezXClassLoader, 
    condition: ConstructorCondition
): Constructor<*>
通过条件查找构造方法

findConstructorOrNull
inline fun Class<*>.findConstructorOrNull(
    noinline condition: ConstructorCondition
): Constructor<*>?
fun Iterable<Constructor<*>>.findConstructorOrNull(
    condition: ConstructorCondition
): Constructor<*>?

fun Array<Constructor<*>>.findConstructorOrNull(
    condition: ConstructorCondition
): Constructor<*>?
扩展函数 通过条件查找构造方法

fun findConstructorOrNull(clz: Class<*>, condition: ConstructorCondition): Constructor<*>?
fun findConstructorOrNull(
    clzName: String, 
    classLoader: ClassLoader = InitFields.ezXClassLoader, 
    condition: ConstructorCondition
): Constructor<*>?
通过条件查找构造方法

findDexClassLoader
inline fun ClassLoader.findDexClassLoader(
    crossinline delegator: (BaseDexClassLoader) -> BaseDexClassLoader = { x -> x }
): BaseDexClassLoader?
取自 哔哩漫游 查找DexClassLoader

findField
fun Array<Field>.findField(condition: FieldCondition): Field
扩展函数 通过条件查找属性

fun Iterable<Field>.findField(condition: FieldCondition): Field
inline fun Class<*>.findField(
    findSuper: Boolean = false, 
    noinline condition: FieldCondition
): Field

fun findField(clz: Class<*>, findSuper: Boolean = false, condition: FieldCondition): Field
fun findField(
    clzName: String, 
    classLoader: ClassLoader = InitFields.ezXClassLoader, 
    findSuper: Boolean = false, 
    condition: FieldCondition
): Field
通过条件查找类中的属性

findFieldObject
fun Any.findFieldObject(findSuper: Boolean = false, condition: FieldCondition): Any
扩展函数 查找符合条件的属性并获取对象

findFieldObjectAs
fun <T> Any.findFieldObjectAs(findSuper: Boolean = false, condition: FieldCondition): T
扩展函数 查找符合条件的属性并获取对象 并转化为T类型

findFieldObjectOrNull
fun Any.findFieldObjectOrNull(findSuper: Boolean = false, condition: FieldCondition): Any?
扩展函数 查找符合条件的属性并获取对象

findFieldObjectOrNullAs
fun <T> Any.findFieldObjectOrNullAs(
    findSuper: Boolean = false, 
    condition: FieldCondition
): T?
扩展函数 查找符合条件的属性并获取对象 并转化为T?类型

findFieldOrNull
fun Array<Field>.findFieldOrNull(condition: FieldCondition): Field?
扩展函数 通过条件查找属性

fun Iterable<Field>.findFieldOrNull(condition: FieldCondition): Field?
inline fun Class<*>.findFieldOrNull(
    findSuper: Boolean = false, 
    noinline condition: FieldCondition
): Field?

fun findFieldOrNull(
    clz: Class<*>, 
    findSuper: Boolean = false, 
    condition: FieldCondition
): Field?
fun findFieldOrNull(
    clzName: String, 
    classLoader: ClassLoader = InitFields.ezXClassLoader, 
    findSuper: Boolean = false, 
    condition: FieldCondition
): Field?
通过条件查找类中的属性

findMethod
fun Array<Method>.findMethod(condition: MethodCondition): Method
扩展函数 通过条件查找方法

fun Iterable<Method>.findMethod(condition: MethodCondition): Method
inline fun Class<*>.findMethod(
    findSuper: Boolean = false, 
    noinline condition: MethodCondition
): Method

fun findMethod(clz: Class<*>, findSuper: Boolean = false, condition: MethodCondition): Method
通过条件查找类中的方法

fun findMethod(
    clzName: String, 
    classLoader: ClassLoader = InitFields.ezXClassLoader, 
    findSuper: Boolean = false, 
    condition: MethodCondition
): Method
通过条件查找方法

findMethodOrNull
fun Array<Method>.findMethodOrNull(condition: MethodCondition): Method?
扩展函数 通过条件查找方法

fun Iterable<Method>.findMethodOrNull(condition: MethodCondition): Method?
inline fun Class<*>.findMethodOrNull(
    findSuper: Boolean = false, 
    noinline condition: MethodCondition
): Method?

fun findMethodOrNull(
    clz: Class<*>, 
    findSuper: Boolean = false, 
    condition: MethodCondition
): Method?
fun findMethodOrNull(
    clzName: String, 
    classLoader: ClassLoader = InitFields.ezXClassLoader, 
    findSuper: Boolean = false, 
    condition: MethodCondition
): Method?
通过条件查找类中的方法

findMethods
fun Array<Pair<Class<*>, MethodCondition>>.findMethods(
    findSuper: Boolean = false
): Array<Method>
扩展函数 通过条件查找数组中对应的方法 每个类只搜索一个方法

fun Iterable<Pair<Class<*>, MethodCondition>>.findMethods(
    findSuper: Boolean = false
): List<Method>
fun Iterable<Class<*>>.findMethods(
    findSuper: Boolean = false, 
    condition: MethodCondition
): List<Method>

fun Array<Class<*>>.findMethods(
    findSuper: Boolean = false, 
    condition: MethodCondition
): Array<Method>
扩展函数 通过条件查找方法 每个类只搜索一个方法

findObject
fun Any.findObject(condition: ObjectCondition): Any?
fun Any.findObject(fieldCond: FieldCondition, objCond: ObjectCondition): Any?
强烈不推荐!!非常慢!!

findStaticObject
fun Class<*>.findStaticObject(condition: ObjectCondition): Any?
fun Any.findStaticObject(fieldCond: FieldCondition, objCond: ObjectCondition): Any?
强烈不推荐!!非常慢!!

findViewByCondition
fun ViewGroup.findViewByCondition(condition: (view: View) -> Boolean): View?
扩展函数 遍历ViewGroup 根据条件查找View

findViewByConditionAs
fun <T : View> ViewGroup.findViewByConditionAs(condition: (view: View) -> Boolean): T?
扩展函数 遍历ViewGroup 根据条件查找View 并将View转换为T?类型

findViewByIdName
fun Activity.findViewByIdName(name: String): View?

fun View.findViewByIdName(name: String): View?
通过名字查找View

forEach
inline fun ViewGroup.forEach(action: (view: View) -> Unit)
扩展函数 遍历ViewGroup

inline fun JSONArray.forEach(action: (Any) -> Unit)
扩展函数 遍历JSONArray

forEachIndexed
inline fun ViewGroup.forEachIndexed(action: (index: Int, view: View) -> Unit)
扩展函数 带index遍历ViewGroup

inline fun JSONArray.forEachIndexed(action: (Int, Any) -> Unit)
扩展函数 遍历JSONArray 并包含索引

getAllClassesList
fun ClassLoader.getAllClassesList(
    delegator: (BaseDexClassLoader) -> BaseDexClassLoader = { loader -> loader }
): List<String>
取自 哔哩漫游 获取所有类名

getAs
fun <T> Field.getAs(obj: Any?): T?
扩展函数 获取对象 并转换为T?类型

getBooleanOrDefault
fun JSONObject.getBooleanOrDefault(key: String, defValue: Boolean = false): Boolean
扩展函数 获取JSONObject中的 Boolean

getBooleanOrNull
fun JSONObject.getBooleanOrNull(key: String): Boolean?
扩展函数 获取JSONObject中的 Boolean

getFieldByDesc
fun ClassLoader.getFieldByDesc(desc: String): Field
扩展函数 通过Descriptor获取属性

fun getFieldByDesc(desc: String, clzLoader: ClassLoader = InitFields.ezXClassLoader): Field
通过Descriptor获取属性

getFieldByDescOrNull
fun ClassLoader.getFieldByDescOrNull(desc: String): Field?
扩展函数 通过Descriptor获取属性

fun getFieldByDescOrNull(
    desc: String, 
    clzLoader: ClassLoader = InitFields.ezXClassLoader
): Field?
通过Descriptor获取属性

getFieldByType
fun Any.getFieldByType(type: Class<*>, isStatic: Boolean = false): Field
扩展函数 通过类型获取属性

getIdByName
fun getIdByName(name: String, ctx: Context = InitFields.appContext): Int
通过名字获取R.id中的组件id

getIntOrDefault
fun JSONObject.getIntOrDefault(key: String, defValue: Int = 0): Int
扩展函数 获取JSONObject中的 Int

getIntOrNull
fun JSONObject.getIntOrNull(key: String): Int?
扩展函数 获取JSONObject中的 Int

getJSONArrayOrEmpty
fun JSONObject.getJSONArrayOrEmpty(key: String): JSONArray
扩展函数 获取JSONObject中的 JSONArray

getJSONArrayOrNull
fun JSONObject.getJSONArrayOrNull(key: String): JSONArray?
扩展函数 获取JSONObject中的 JSONArray

getLongOrDefault
fun JSONObject.getLongOrDefault(key: String, defValue: Long = 0): Long
扩展函数 获取JSONObject中的 Long

getLongOrNull
fun JSONObject.getLongOrNull(key: String): Long?
扩展函数 获取JSONObject中的 Long

getMethodByDesc
fun ClassLoader.getMethodByDesc(desc: String): Method
扩展函数 通过Descriptor获取方法

fun getMethodByDesc(desc: String, clzLoader: ClassLoader = InitFields.ezXClassLoader): Method
通过Descriptor获取方法

getMethodByDescOrNull
fun ClassLoader.getMethodByDescOrNull(desc: String): Method?
扩展函数 通过Descriptor获取方法

fun getMethodByDescOrNull(
    desc: String, 
    clzLoader: ClassLoader = InitFields.ezXClassLoader
): Method?
通过Descriptor获取方法

getNonNull
fun Field.getNonNull(obj: Any?): Any
扩展函数 获取非空对象

getNonNullAs
fun <T> Field.getNonNullAs(obj: Any?): T
扩展函数 获取非空对象 并转换为T类型

getObject
fun Any.getObject(objName: String, type: Class<*>? = null): Any
扩展函数 获取实例化对象中的对象

getObjectAs
fun <T> Any.getObjectAs(field: Field): T

fun <T> Any.getObjectAs(objName: String, type: Class<*>? = null): T
扩展函数 获取实例化对象中的对象 并转化为类型T

getObjectByType
fun Any.getObjectByType(type: Class<*>): Any
扩展函数 通过类型 获取实例化对象中的对象

getObjectByTypeAs
fun <T> Any.getObjectByTypeAs(type: Class<*>): T
扩展函数 通过类型 获取实例化对象中的对象 并转换为T类型

getObjectOrNull
fun Any.getObjectOrNull(field: Field): Any?
fun Any.getObjectOrNull(objName: String, type: Class<*>? = null): Any?
扩展函数 获取实例化对象中的对象

fun JSONObject.getObjectOrNull(key: String): Any?
扩展函数 获取JSONObject中的 Object

getObjectOrNullAs
fun <T> Any.getObjectOrNullAs(field: Field): T?
扩展函数 获取实例化对象中的对象 并且转换为T?类型

fun <T> Any.getObjectOrNullAs(objName: String, type: Class<*>? = null): T?
扩展函数 获取实例化对象中的对象

getObjectOrNullByType
fun Any.getObjectOrNullByType(type: Class<*>): Any?
扩展函数 通过类型 获取实例化对象中的对象

getObjectOrNullByTypeAs
fun <T> Any.getObjectOrNullByTypeAs(type: Class<*>): T?
扩展函数 通过类型 获取实例化对象中的对象 并转换为T?类型

getStatic
fun Field.getStatic(): Any?
扩展函数 获取静态对象

getStaticAs
fun <T> Field.getStaticAs(): T?
扩展函数 获取静态对象 并转换为T?类型

getStaticFieldByType
fun Any.getStaticFieldByType(type: Class<*>): Field
getStaticNonNull
fun Field.getStaticNonNull(): Any
扩展函数 获取静态非空对象

getStaticNonNullAs
fun <T> Field.getStaticNonNullAs(): T
扩展函数 获取静态非空对象 并转换为T类型

getStaticObject
fun getStaticObject(field: Field): Any
获取Field中的对象

fun Class<*>.getStaticObject(objName: String, type: Class<*>? = null): Any
扩展函数 获取类中的静态对象

getStaticObjectAs
fun <T> getStaticObjectAs(field: Field): T
获取Field中的对象 并转换为T类型

fun <T> Class<*>.getStaticObjectAs(objName: String, type: Class<*>? = null): T
扩展函数 获取类中的静态对象 并转换为T类型

getStaticObjectByType
fun Class<*>.getStaticObjectByType(type: Class<*>): Any
扩展函数 通过类型 获取类中的静态对象

getStaticObjectByTypeAs
fun <T> Class<*>.getStaticObjectByTypeAs(type: Class<*>): T
扩展函数 通过类型 获取类中的静态对象 并转换为T类型

getStaticObjectOrNull
fun getStaticObjectOrNull(field: Field): Any?
获取Field中的对象

fun Class<*>.getStaticObjectOrNull(objName: String, type: Class<*>? = null): Any?
扩展函数 获取类中的静态对象

getStaticObjectOrNullAs
fun <T> getStaticObjectOrNullAs(field: Field): T?
获取Field中的对象 并转换为T?类型

fun <T> Class<*>.getStaticObjectOrNullAs(objName: String, type: Class<*>? = null): T?
扩展函数 获取类中的静态对象 并且转换为T?类型

getStaticObjectOrNullByType
fun Class<*>.getStaticObjectOrNullByType(type: Class<*>): Any?
扩展函数 通过类型 获取类中的静态对象

getStaticObjectOrNullByTypeAs
fun <T> Class<*>.getStaticObjectOrNullByTypeAs(type: Class<*>): T?
扩展函数 通过类型 获取类中的静态对象 并转换为T？类型

getStringOrDefault
fun JSONObject.getStringOrDefault(key: String, defValue: String = ""): String
扩展函数 获取JSONObject中的 String

getStringOrNull
fun JSONObject.getStringOrNull(key: String): String?
扩展函数 获取JSONObject中的 String

hookAfter
fun Constructor<*>.hookAfter(hooker: Hooker): XC_MethodHook.Unhook
fun Method.hookAfter(hooker: Hooker): XC_MethodHook.Unhook
fun Array<Constructor<*>>.hookAfter(hooker: Hooker): Array<XC_MethodHook.Unhook>
fun Array<Method>.hookAfter(hooker: Hooker): Array<XC_MethodHook.Unhook>
@JvmName(name = "hookConstructorAfter")
fun Iterable<Constructor<*>>.hookAfter(hooker: Hooker): List<XC_MethodHook.Unhook>
fun Iterable<Method>.hookAfter(hooker: Hooker): List<XC_MethodHook.Unhook>
@JvmName(name = "hookConstructorAfter")
fun Iterable<Constructor<*>>.hookAfter(
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    hooker: Hooker
): List<XC_MethodHook.Unhook>
fun Iterable<Method>.hookAfter(
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    hooker: Hooker
): List<XC_MethodHook.Unhook>

fun Constructor<*>.hookAfter(
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    hooker: Hooker
): XC_MethodHook.Unhook
扩展函数 hook构造执行后

fun Method.hookAfter(
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    hooker: Hooker
): XC_MethodHook.Unhook
扩展函数 hook方法执行后

fun Array<Constructor<*>>.hookAfter(
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    hooker: Hooker
): Array<XC_MethodHook.Unhook>
扩展函数 hook多个构造执行后

fun Array<Method>.hookAfter(
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    hooker: Hooker
): Array<XC_MethodHook.Unhook>
扩展函数 hook多个方法执行后

hookAllConstructorAfter
fun Class<*>.hookAllConstructorAfter(hooker: Hooker): Array<XC_MethodHook.Unhook>

fun Class<*>.hookAllConstructorAfter(
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    hooker: Hooker
): Array<XC_MethodHook.Unhook>
扩展函数 hook类的所有构造后

fun hookAllConstructorAfter(
    clzName: String, 
    clzLoader: ClassLoader = InitFields.ezXClassLoader, 
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    hooker: Hooker
): Array<XC_MethodHook.Unhook>
hook类的所有构造后

hookAllConstructorBefore
fun Class<*>.hookAllConstructorBefore(hooker: Hooker): Array<XC_MethodHook.Unhook>

fun Class<*>.hookAllConstructorBefore(
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    hooker: Hooker
): Array<XC_MethodHook.Unhook>
扩展函数 hook类的所有构造前

fun hookAllConstructorBefore(
    clzName: String, 
    clzLoader: ClassLoader = InitFields.ezXClassLoader, 
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    hooker: Hooker
): Array<XC_MethodHook.Unhook>
hook类的所有构造前

hookAllConstructorReplace
fun Class<*>.hookAllConstructorReplace(hooker: ReplaceHooker): Array<XC_MethodHook.Unhook>

fun Class<*>.hookAllConstructorReplace(
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    hooker: ReplaceHooker
): Array<XC_MethodHook.Unhook>
扩展函数 替换类的所有构造

fun hookAllConstructorReplace(
    clzName: String, 
    clzLoader: ClassLoader = InitFields.ezXClassLoader, 
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    hooker: Hooker
): Array<XC_MethodHook.Unhook>
替换类的所有构造

hookBefore
fun Constructor<*>.hookBefore(hooker: Hooker): XC_MethodHook.Unhook
fun Method.hookBefore(hooker: Hooker): XC_MethodHook.Unhook
fun Array<Constructor<*>>.hookBefore(hooker: Hooker): Array<XC_MethodHook.Unhook>
fun Array<Method>.hookBefore(hooker: Hooker): Array<XC_MethodHook.Unhook>
@JvmName(name = "hookConstructorBefore")
fun Iterable<Constructor<*>>.hookBefore(hooker: Hooker): List<XC_MethodHook.Unhook>
fun Iterable<Method>.hookBefore(hooker: Hooker): List<XC_MethodHook.Unhook>
@JvmName(name = "hookConstructorBefore")
fun Iterable<Constructor<*>>.hookBefore(
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    hooker: Hooker
): List<XC_MethodHook.Unhook>
fun Iterable<Method>.hookBefore(
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    hooker: Hooker
): List<XC_MethodHook.Unhook>

fun Constructor<*>.hookBefore(
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    hook: Hooker
): XC_MethodHook.Unhook
扩展函数 hook构造执行前

fun Method.hookBefore(
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    hook: Hooker
): XC_MethodHook.Unhook
扩展函数 hook方法执行前

fun Array<Constructor<*>>.hookBefore(
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    hooker: Hooker
): Array<XC_MethodHook.Unhook>
扩展函数 hook多个构造执行前

fun Array<Method>.hookBefore(
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    hooker: Hooker
): Array<XC_MethodHook.Unhook>
扩展函数 hook多个方法执行前

hooker
inline fun hooker(crossinline hookCallback: Hooker): Hooker
hookMethod
fun Constructor<*>.hookMethod(hookCallback: XC_MethodHook): XC_MethodHook.Unhook
@JvmName(name = "hookConstructor")
fun Iterable<Constructor<*>>.hookMethod(
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    hooker: XposedHookFactory.() -> Unit
): List<XC_MethodHook.Unhook>
fun Iterable<Method>.hookMethod(
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    hook: XposedHookFactory.() -> Unit
): List<XC_MethodHook.Unhook>

fun Method.hookMethod(hookCallback: XC_MethodHook): XC_MethodHook.Unhook
扩展函数 hook方法/构造

fun Constructor<*>.hookMethod(
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    hook: XposedHookFactory.() -> Unit
): XC_MethodHook.Unhook
扩展函数 hook构造 直接以

fun Method.hookMethod(
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    hook: XposedHookFactory.() -> Unit
): XC_MethodHook.Unhook
扩展函数 hook方法 直接以

fun Array<Constructor<*>>.hookMethod(
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    hooker: XposedHookFactory.() -> Unit
): Array<XC_MethodHook.Unhook>
扩展函数 hook多个构造 直接以

fun Array<Method>.hookMethod(
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    hook: XposedHookFactory.() -> Unit
): Array<XC_MethodHook.Unhook>
扩展函数 hook多个方法 直接以

hookReplace
fun Constructor<*>.hookReplace(hooker: ReplaceHooker): XC_MethodHook.Unhook
fun Method.hookReplace(hooker: ReplaceHooker): XC_MethodHook.Unhook
fun Array<Constructor<*>>.hookReplace(hooker: ReplaceHooker): Array<XC_MethodHook.Unhook>
fun Array<Method>.hookReplace(hooker: ReplaceHooker): Array<XC_MethodHook.Unhook>
@JvmName(name = "hookConstructorReplace")
fun Iterable<Constructor<*>>.hookReplace(hooker: ReplaceHooker): List<XC_MethodHook.Unhook>
fun Iterable<Method>.hookReplace(hooker: ReplaceHooker): List<XC_MethodHook.Unhook>
@JvmName(name = "hookConstructorReplace")
fun Iterable<Constructor<*>>.hookReplace(
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    hooker: ReplaceHooker
): List<XC_MethodHook.Unhook>
fun Iterable<Method>.hookReplace(
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    hooker: ReplaceHooker
): List<XC_MethodHook.Unhook>

fun Constructor<*>.hookReplace(
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    hooker: ReplaceHooker
): XC_MethodHook.Unhook
扩展函数 替换构造

fun Method.hookReplace(
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    hook: ReplaceHooker
): XC_MethodHook.Unhook
扩展函数 替换方法

fun Array<Constructor<*>>.hookReplace(
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    hooker: ReplaceHooker
): Array<XC_MethodHook.Unhook>
扩展函数 替换多个构造

fun Array<Method>.hookReplace(
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    hooker: ReplaceHooker
): Array<XC_MethodHook.Unhook>
扩展函数 替换多个方法

hookReturnConstant
fun Constructor<*>.hookReturnConstant(obj: Any?): XC_MethodHook.Unhook
fun Method.hookReturnConstant(obj: Any?): XC_MethodHook.Unhook
fun Array<Constructor<*>>.hookReturnConstant(obj: Any?): Array<XC_MethodHook.Unhook>
fun Array<Method>.hookReturnConstant(obj: Any?): Array<XC_MethodHook.Unhook>
@JvmName(name = "hookConstructorReturnConstant")
fun List<Constructor<*>>.hookReturnConstant(obj: Any?): List<XC_MethodHook.Unhook>
fun List<Method>.hookReturnConstant(obj: Any?): List<XC_MethodHook.Unhook>
fun Array<Constructor<*>>.hookReturnConstant(
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    obj: Any?
): Array<XC_MethodHook.Unhook>
@JvmName(name = "hookConstructorReturnConstant")
fun List<Constructor<*>>.hookReturnConstant(
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    obj: Any?
): List<XC_MethodHook.Unhook>
fun List<Method>.hookReturnConstant(
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    obj: Any?
): List<XC_MethodHook.Unhook>

fun Constructor<*>.hookReturnConstant(
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    obj: Any?
): XC_MethodHook.Unhook
扩展函数 hook构造 使其直接返回一个值

fun Method.hookReturnConstant(
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    obj: Any?
): XC_MethodHook.Unhook
扩展函数 hook方法 使其直接返回一个值

fun Array<Method>.hookReturnConstant(
    priority: Int = XCallback.PRIORITY_DEFAULT, 
    obj: Any?
): Array<XC_MethodHook.Unhook>
扩展函数 hook方法数组中的所有方法 使其直接返回一个值

invokeAs
fun <T> Method.invokeAs(obj: Any?, vararg args: Any?): T?
扩展函数 调用方法 并将返回值转换为T?类型

invokeMethod
fun Any.invokeMethod(vararg args: Any?, condition: MethodCondition): Any?
扩展函数 调用对象中符合条件的方法

fun Any.invokeMethod(
    methodName: String, 
    args: Args = args(), 
    argTypes: ArgTypes = argTypes(), 
    returnType: Class<*>? = null
): Any?
扩展函数 调用对象的方法

invokeMethodAs
fun <T> Any.invokeMethodAs(
    methodName: String, 
    args: Args = args(), 
    argTypes: ArgTypes = argTypes(), 
    returnType: Class<*>? = null
): T?
扩展函数 调用对象的方法 并且将返回值转换为T?类型

invokeMethodAuto
fun Any.invokeMethodAuto(methodName: String, vararg args: Any?): Any?
扩展函数 调用对象与形参表最佳匹配的方法

invokeMethodAutoAs
fun <T> Any.invokeMethodAutoAs(methodName: String, vararg args: Any?): T?
扩展函数 调用对象与形参表最佳匹配的方法 并将返回值转换为T?类型

invokeStaticMethod
fun Class<*>.invokeStaticMethod(vararg args: Any?, condition: MethodCondition): Any?
扩展函数 调用类中符合条件的静态方法

fun Class<*>.invokeStaticMethod(
    methodName: String, 
    args: Args = args(), 
    argTypes: ArgTypes = argTypes(), 
    returnType: Class<*>? = null
): Any?
扩展函数 调用类的静态方法

invokeStaticMethodAs
fun <T> Class<*>.invokeStaticMethodAs(
    methodName: String, 
    args: Args = args(), 
    argTypes: ArgTypes = argTypes(), 
    returnType: Class<*>? = null
): T?
扩展函数 调用类的静态方法 并且将返回值转换为T?类型

invokeStaticMethodAuto
fun Class<*>.invokeStaticMethodAuto(methodName: String, vararg args: Any?): Any?
扩展函数 调用类中与形参表最佳匹配的静态方法

invokeStaticMethodAutoAs
fun <T> Class<*>.invokeStaticMethodAutoAs(methodName: String, vararg args: Any?): T?
扩展函数 调用类中与形参表最佳匹配的静态方法 并将返回值转换为T?类型

isChildClassOf
fun Class<*>.isChildClassOf(
    clzName: String, 
    clzLoader: ClassLoader = InitFields.ezXClassLoader
): Boolean
扩展函数 判断自身是否为某个类的子类

isEmpty
fun ViewGroup.isEmpty(): Boolean
扩展函数 判断ViewGroup是否为空

isNotEmpty
fun ViewGroup.isNotEmpty(): Boolean
扩展函数 判断ViewGroup是否不为空

loadAllClasses
fun Array<String>.loadAllClasses(
    clzLoader: ClassLoader = InitFields.ezXClassLoader
): Array<Class<*>>
扩展函数 加载数组中的所有类

fun Iterable<String>.loadAllClasses(
    clzLoader: ClassLoader = InitFields.ezXClassLoader
): List<Class<*>>
loadAndFindAllMethods
fun Array<Pair<String, MethodCondition>>.loadAndFindAllMethods(
    classLoader: ClassLoader = InitFields.ezXClassLoader, 
    findSuper: Boolean = false
): Array<Method>
扩展函数 加载数组中的类并且通过条件查找方法

fun Iterable<Pair<String, MethodCondition>>.loadAndFindAllMethods(
    classLoader: ClassLoader = InitFields.ezXClassLoader, 
    findSuper: Boolean = false
): List<Method>
fun Iterable<String>.loadAndFindAllMethods(
    classLoader: ClassLoader = InitFields.ezXClassLoader, 
    findSuper: Boolean = false, 
    condition: MethodCondition
): List<Method>

fun Array<String>.loadAndFindAllMethods(
    classLoader: ClassLoader = InitFields.ezXClassLoader, 
    findSuper: Boolean = false, 
    condition: MethodCondition
): Array<Method>
扩展函数 加载数组中的类并且通过条件查找所有方法

loadAndFindMethods
fun Array<Pair<String, MethodCondition>>.loadAndFindMethods(
    classLoader: ClassLoader = InitFields.ezXClassLoader, 
    findSuper: Boolean = false
): Array<Method>
fun Array<String>.loadAndFindMethods(
    classLoader: ClassLoader = InitFields.ezXClassLoader, 
    findSuper: Boolean = false, 
    condition: MethodCondition
): Array<Method>
扩展函数 加载数组中的类并且通过条件查找方法 每个类只搜索一个方法

fun Iterable<Pair<String, MethodCondition>>.loadAndFindMethods(
    classLoader: ClassLoader = InitFields.ezXClassLoader, 
    findSuper: Boolean = false
): List<Method>
fun Iterable<String>.loadAndFindMethods(
    classLoader: ClassLoader = InitFields.ezXClassLoader, 
    findSuper: Boolean = false, 
    condition: MethodCondition
): List<Method>
loadClass
fun loadClass(clzName: String, clzLoader: ClassLoader = InitFields.ezXClassLoader): Class<*>
通过模块加载类

loadClassAny
@JvmName(name = "loadClassAnyFromArray")
fun Array<String>.loadClassAny(clzLoader: ClassLoader = InitFields.ezXClassLoader): Class<*>
尝试加载数组中的一个类

fun Iterable<String>.loadClassAny(
    clzLoader: ClassLoader = InitFields.ezXClassLoader
): Class<*>

fun loadClassAny(
    vararg clzName: String, 
    clzLoader: ClassLoader = InitFields.ezXClassLoader
): Class<*>
尝试加载列表中的一个类

loadClassAnyOrNull
@JvmName(name = "loadClassAnyOrFromList")
fun Array<String>.loadClassAnyOrNull(
    clzLoader: ClassLoader = InitFields.ezXClassLoader
): Class<*>?
尝试加载数组中的一个类 失败则返回null

fun Iterable<String>.loadClassAnyOrNull(
    clzLoader: ClassLoader = InitFields.ezXClassLoader
): Class<*>?

fun loadClassAnyOrNull(
    vararg clzName: String, 
    clzLoader: ClassLoader = InitFields.ezXClassLoader
): Class<*>?
尝试加载列表中的一个类 失败则返回null

loadClassesIfExists
fun Array<String>.loadClassesIfExists(
    clzLoader: ClassLoader = InitFields.ezXClassLoader
): Array<Class<*>>
扩展函数 尝试加载数组中的所有类

fun Iterable<String>.loadClassesIfExists(
    clzLoader: ClassLoader = InitFields.ezXClassLoader
): List<Class<*>>
loadClassOrNull
fun loadClassOrNull(
    clzName: String, 
    clzLoader: ClassLoader = InitFields.ezXClassLoader
): Class<*>?
尝试加载一个类 如果失败则返回null

map
inline fun JSONArray.map(transform: (Any) -> Any): JSONArray
扩展函数 对JSONArray进行转换 并返回新的JSONArray

mapToList
inline fun <T> JSONArray.mapToList(transform: (Any) -> T): List<T>
扩展函数 对JSONArray进行转换 并返List

mcp
infix fun Class<*>.mcp(condition: MethodCondition): Pair<Class<*>, MethodCondition>
infix fun String.mcp(condition: MethodCondition): Pair<String, MethodCondition>
method
fun Any.method(
    methodName: String, 
    returnType: Class<*>? = null, 
    isStatic: Boolean = false, 
    argTypes: ArgTypes = argTypes()
): Method
扩展函数 通过类或者对象获取单个方法

newInstance
fun Class<*>.newInstance(args: Args = args(), argTypes: ArgTypes = argTypes()): Any?
扩展函数 创建新的实例化对象

newInstanceAs
fun <T> Class<*>.newInstanceAs(args: Args = args(), argTypes: ArgTypes = argTypes()): T?
扩展函数 创建新的实例化对象 并将对象转换为T?类型

onEach
inline fun JSONArray.onEach(action: (Any) -> Unit): JSONArray
扩展函数 遍历JSONArray 并返回同一个JSONArray

onEachIndexed
inline fun JSONArray.onEachIndexed(action: (Int, Any) -> Unit): JSONArray
扩展函数 遍历JSONArray 包含索引 并返回同一个JSONArray

postOnMainThread
fun Runnable.postOnMainThread()
扩展函数 将函数放到主线程执行 如UI更新、显示Toast等

putObject
fun Any.putObject(field: Field, value: Any?)
fun Any.putObject(objName: String, value: Any?, fieldType: Class<*>? = null)
扩展函数 设置对象中对象的值

putObjectByType
fun Any.putObjectByType(value: Any?, type: Class<*>)
扩展函数 通过类型设置值

putStaticObject
fun Class<*>.putStaticObject(objName: String, value: Any?, fieldType: Class<*>? = null)
扩展函数 设置类中静态对象值

putStaticObjectByType
fun Class<*>.putStaticObjectByType(value: Any?, type: Class<*>)
扩展函数 通过类型设置类中的静态对象的值

removeIf
inline fun <K, V> MutableMap<K, V>.removeIf(predicate: (K, V) -> Boolean)
扩展函数 移除可变字典中符合条件的元素

replaceHooker
inline fun replaceHooker(crossinline hookCallback: ReplaceHooker): ReplaceHooker
restartHostApp
fun restartHostApp(activity: Activity)
重新启动宿主App

retainIf
inline fun <E> MutableList<E>.retainIf(predicate: (E) -> Boolean)
扩展函数 保留可变列表中符合条件的元素

inline fun <K, V> MutableMap<K, V>.retainIf(predicate: (K, V) -> Boolean)
扩展函数 保留可变字典中符合条件的元素

inline fun <E> MutableSet<E>.retainIf(predicate: (E) -> Boolean)
扩展函数 保留可变集合中符合条件的元素

runOnMainThread
fun runOnMainThread(runnable: Runnable)
sameAs
fun Array<Class<*>>.sameAs(vararg other: Any): Boolean
扩展函数 判断类是否相同(用于判断参数)

fun List<Class<*>>.sameAs(vararg other: Any): Boolean
setViewZeroSize
fun View.setViewZeroSize()
扩展函数 将View布局的高度和宽度设置为0

showToast
fun Context.showToast(msg: String, length: Int = Toast.LENGTH_SHORT)
fun Context.showToast(msg: String, vararg args: Any?, length: Int = Toast.LENGTH_SHORT)
扩展函数 显示一个Toast

staticField
fun Class<*>.staticField(fieldName: String, type: Class<*>? = null): Field
扩展函数 通过类获取静态属性

staticMethod
fun Class<*>.staticMethod(
    methodName: String, 
    returnType: Class<*>? = null, 
    argTypes: ArgTypes = argTypes()
): Method
扩展函数 通过类获取单个静态方法

tryOrFalse
inline fun tryOrFalse(block: () -> Unit): Boolean
尝试执行一块代码，如果成功返true，失败则返回false

tryOrLog
inline fun tryOrLog(block: () -> Unit)
尝试执行一块代码，如果失败则记录日志

tryOrLogFalse
inline fun tryOrLogFalse(block: () -> Unit): Boolean
尝试执行一块代码，如果成功返true，失败则返回false并且记录日志

tryOrLogNull
inline fun <T> tryOrLogNull(block: () -> T?): T?
尝试执行一块代码，如果成功返回代码块执行的结果，失败则返回null并且记录日志

tryOrNull
inline fun <T> tryOrNull(block: () -> T?): T?
尝试执行一块代码，如果成功返回代码块执行的结果，失败则返回null

unhookAll
fun Array<XC_MethodHook.Unhook>.unhookAll()
执行数组中所有的unhook

fun Iterable<XC_MethodHook.Unhook>.unhookAll()