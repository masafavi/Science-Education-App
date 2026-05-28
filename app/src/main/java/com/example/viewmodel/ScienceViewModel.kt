package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.database.ScienceDatabase
import com.example.data.model.ChatMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AppTab {
    HOME, VIRTUAL_LAB, AI_TEACHER, CHANNELS
}

// Lesson Category
enum class LessonCategory(val farsiName: String) {
    ALL("همه دروس"),
    PHYSICS("فیزیک"),
    CHEMISTRY("شیمی"),
    BIOLOGY("زیست‌شناسی"),
    GEOLOGY("زمین‌شناسی")
}

// Lesson Topic
data class LessonTopic(
    val id: String,
    val title: String,
    val description: String,
    val summary: String,
    val category: LessonCategory,
    val difficulty: String, // "آسان", "متوسط", "پیشرفته"
    val duration: String,
    val keyConcepts: List<String>,
    val imageUrl: String = ""
)

// Curated Video representation
data class AcademicVideo(
    val id: String,
    val title: String,
    val channelName: String,
    val duration: String,
    val views: String,
    val category: String,
    val url: String = "",
    val description: String = ""
)

class ScienceViewModel(application: Application) : AndroidViewModel(application) {

    private val db = ScienceDatabase.getDatabase(application)
    private val chatDao = db.chatDao()

    // Active Navigation
    private val _activeTab = MutableStateFlow(AppTab.HOME)
    val activeTab: StateFlow<AppTab> = _activeTab.asStateFlow()

    fun selectTab(tab: AppTab) {
        _activeTab.value = tab
    }

    // --- Home Tab State ---
    private val _selectedCategory = MutableStateFlow(LessonCategory.ALL)
    val selectedCategory: StateFlow<LessonCategory> = _selectedCategory.asStateFlow()

    fun selectCategory(category: LessonCategory) {
        _selectedCategory.value = category
    }

    private val _selectedLesson = MutableStateFlow<LessonTopic?>(null)
    val selectedLesson: StateFlow<LessonTopic?> = _selectedLesson.asStateFlow()

    fun selectLesson(lesson: LessonTopic?) {
        _selectedLesson.value = lesson
    }

    val lessonTopics = listOf(
        LessonTopic(
            id = "atom_struct",
            title = "ساختار اتم و ذرات سازنده",
            description = "آشنایی با دنیای شگفت‌انگیز اتم‌ها، مدل اتمی بور و ذرات زیراتمی پروتون، نوترون و الکترون.",
            summary = "اتم کوچک‌ترین واحد تشکیل‌دهنده ماده است. طبق مدل بور، الکترون‌ها در مدارهای مشخص به دور هسته متراکم اتم (شامل پروتون‌های مثبت و نوترون‌های خنثی) در حال گردش هستند. تعداد پروتون‌ها (عدد اتمی) نشان‌دهنده هویت عنصر شیمیایی است.",
            category = LessonCategory.CHEMISTRY,
            difficulty = "متوسط",
            duration = "۳۵ دقیقه",
            keyConcepts = listOf("مدل اتمی بور", "پروتون و نوترون", "ترازهای انرژی الکترونی", "عدد اتمی")
        ),
        LessonTopic(
            id = "prism_refract",
            title = "شکست و تجزیه نور در منشور",
            description = "بررسی نحوه خم شدن ذرات نور هنگام عبور از رساناهای مختلف و راز پیدایش رنگ‌های رنگین‌کمان کات کبود.",
            summary = "هنگامی که نور سفید خورشید با زاویه به منشور شیشه‌ای می‌تابد، به دلیل تفاوت در طول موج رنگ‌های مختلف، هر کدام با زاویه‌ای متفاوت منکسر (شکسته) می‌شوند. این تفاوت در انکسار، نور سفید را به هفت رنگ طیف مرئی تجزیه می‌کند. به این پدیده پاشش نور می‌گویند.",
            category = LessonCategory.PHYSICS,
            difficulty = "متوسط",
            duration = "۲۸ دقیقه",
            keyConcepts = listOf("قانون انکسار اسنل", "انحراف نور (Refraction)", "طیف‌های رنگین‌کمانی", "طول موج نوری")
        ),
        LessonTopic(
            id = "volcano_reaction",
            title = "واکنش اسید و باز (آتشفشان گاز)",
            description = "آشنایی با پدیده خنثی‌سازی اسیدها و بازها و شبیه‌سازی فوران آتشفشان کربن دی‌اکسید زنده.",
            summary = "وقتی یک اسید (مانند سرکه یا استیک اسید) با یک باز (بر پایه سدیم بی‌کربنات یا جوش شیرین) واکنش می‌دهد، فرایند خنثی‌سازی صورت می‌گیرد. این واکنش منجر به تولید نمک، آب و گاز دی‌اکسید کربن (CO2) می‌شود. خروج سریع گازها حباب‌های عظیمی ایجاد کرده و شبیه فوران گدازه آتشفشان عمل می‌کند.",
            category = LessonCategory.CHEMISTRY,
            difficulty = "آسان",
            duration = "۲۰ دقیقه",
            keyConcepts = listOf("خنثی‌سازی اسیدی", "تولید کربن دی‌اکسید", "سرکه و جوش شیرین", "محلول‌های بافری")
        ),
        LessonTopic(
            id = "elec_circuit",
            title = "الکتریسیته و قوانین مدارها",
            description = "یادگیری جریان یافتن بارهای الکتریکی درون مدارها، تفاوت اتصال موازی و سری و قانون بنیادی اهم.",
            summary = "مدار الکتریکی مسیری بسته برای عبور جریان الکتریسیته است. جریان الکتریکی بستگی تام به پتانسیل منبع (ولتاژ) و مقاومت کل مدار دارد که با فرمول اهم (V = I * R) توصیف می‌شود. در مدار سری، قطع یک لامپ، کل مدار را خاموش می‌کند اما در مدار موازی هر لامپ مسیر مستقلی دارد.",
            category = LessonCategory.PHYSICS,
            difficulty = "پیشرفته",
            duration = "۴۲ دقیقه",
            keyConcepts = listOf("قانون جادویی اهم", "مدار سری و موازی", "شدت جریان الکتریکی", "مقاومت و رسانایی")
        ),
        LessonTopic(
            id = "cell_genetics",
            title = "ساختار سلولی و رازهای ژنتیک DNA",
            description = "سفری به داخل کارخانه زندگی، کلروفیل گیاهی، کدهای وراثت گمشده و ژن‌های کروموزومی انسان.",
            summary = "سلول واحد اصلی حیات است. هسته در درون سلول‌های واجد غشاء، فرماندهی را بر عهده دارد و حاوی کروموزوم‌ها و زنجیره DNA است. این مولکول خارق‌العاده، تمام دستورالعمل‌های ژنتیکی و وراثتی موجود زنده را در قالب کدهای نوکلئوتیدی در زمان تکثیر سلولی منتقل می‌کند.",
            category = LessonCategory.BIOLOGY,
            difficulty = "پیشرفته",
            duration = "۵۰ دقیقه",
            keyConcepts = listOf("ساختار سلولی گیاهی و جانوری", "زنجیره اطلاعاتی DNA", "کروموزوم‌ها و وراثت", "میتوز و میوز")
        ),
        LessonTopic(
            id = "water_cycle",
            title = "چرخه آب و تکامل زمین",
            description = "کاوش در سیستم پویای حرکت مداوم آب بین ابرها، اقیانوس‌ها و ذخایر پنهان زیرزمین با ارزش بالا.",
            summary = "آب روی کره زمین دائماً در حال گردش است. نور خورشید آب اقیانوس‌ها را تبخیر کرده، در طبقات بالا متراکم (میعان) شده و به صورت باران، تگرگ یا برف بازمی‌گردد. بخشی از آب‌های فروریخته وارد سفره‌های آب‌های زیرزمینی شده و باز از رودخانه‌ها سر بر می‌آورند تا تعادل کره زمین همواره حفظ شود.",
            category = LessonCategory.GEOLOGY,
            difficulty = "آسان",
            duration = "۱۸ دقیقه",
            keyConcepts = listOf("تبخیر و تعرق مکرر", "میعان در ابرها", "نفوذپذیری خاک", "حفاظت از منابع آب شیرین")
        )
    )

    // --- Virtual Lab Simulation State ---
    val availableLabs = listOf(
        "ساختار اتم الکترومغناطیسی",
        "شکست نور در منشور اپتیکی",
        "شبیه‌ساز واکنش آتشفشان شیمی",
        "مدار الکتریکی سری و موازی"
    )
    private val _currentLab = MutableStateFlow(availableLabs[0])
    val currentLab: StateFlow<String> = _currentLab.asStateFlow()

    fun selectLab(labName: String) {
        _currentLab.value = labName
        resetLabSliders(labName)
    }

    // Interactive Slider States
    var atomProtons = mutableStateOf(3) // Lithium
    var atomElectrons = mutableStateOf(3)
    var atomOrbitScale = mutableStateOf(1.0f)

    var prismAngle = mutableStateOf(45f)
    var prismWaveType = mutableStateOf("سفید") // سفید، قرمز، سبز، آبی

    var acidAmount = mutableStateOf(50f) // ml
    var sodaAmount = mutableStateOf(10f) // grams
    var reactionActive = mutableStateOf(false)
    var reactionBubblesCount = mutableStateOf(0)

    var circuitVoltage = mutableStateOf(9f) // Volts
    var circuitResistance = mutableStateOf(10f) // Ohms
    var circuitParallelMode = mutableStateOf(false)

    var isArView = mutableStateOf(false) // Toggle simulated camera AR scanner

    private fun resetLabSliders(labName: String) {
        when (labName) {
            "ساختار اتم الکترومغناطیسی" -> {
                atomProtons.value = 3
                atomElectrons.value = 3
                atomOrbitScale.value = 1.0f
            }
            "شکست نور در منشور اپتیکی" -> {
                prismAngle.value = 45f
                prismWaveType.value = "سفید"
            }
            "شبیه‌ساز واکنش آتشفشان شیمی" -> {
                acidAmount.value = 50f
                sodaAmount.value = 10f
                reactionActive.value = false
                reactionBubblesCount.value = 0
            }
            "مدار الکتریکی سری و موازی" -> {
                circuitVoltage.value = 9f
                circuitResistance.value = 10f
                circuitParallelMode.value = false
            }
        }
    }

    // --- AI Teacher Tab State ---
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        // Collect messages from Room DB flow to populate chat Messages
        viewModelScope.launch {
            chatDao.getAllMessages()
                .catch { e ->
                    Log.e("ScienceViewModel", "Errors loading messages: ${e.message}")
                }
                .collect { messages ->
                    if (messages.isEmpty()) {
                        // Insert standard welcome message from AI teacher
                        val welcomeMessage = ChatMessage(
                            sender = "teacher",
                            text = "سلام دانشمند جوان! 🌟 من معلم همراه علوم تجربی شما، 'استاد دانشمند' هستم. خوشحالم که برای کشف رازهای شگفت‌انگیز زیست، فیزیک، شیمی و زمین‌شناسی کنار همیم.\n\nهر سوالی درباره درس‌ها، فرمول‌ها یا آزمایش‌های امروز داری بپرس تا با زبون ساده برات قورتش بدم! 📝"
                        )
                        chatDao.insertMessage(welcomeMessage)
                    } else {
                        _chatMessages.value = messages
                    }
                }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            chatDao.clearHistory()
        }
    }

    fun askTeacher(question: String) {
        if (question.trim().isEmpty() || _isGenerating.value) return

        viewModelScope.launch {
            // 1. Save user question
            val userMsg = ChatMessage(sender = "student", text = question)
            chatDao.insertMessage(userMsg)
            _isGenerating.value = true
            _errorMessage.value = null

            // 2. Query Gemini API
            val responseText = queryGemini(question)

            // 3. Save response from AI teacher
            val teacherMsg = ChatMessage(sender = "teacher", text = responseText)
            chatDao.insertMessage(teacherMsg)
            _isGenerating.value = false
        }
    }

    private suspend fun queryGemini(prompt: String): String = withContext(Dispatchers.IO) {
        val key = BuildConfig.GEMINI_API_KEY
        // System response prefix instructions
        val systemPrompt = "تو یک معلم مهربان، باهوش و بسیار صمیمی علوم تجربی مدارس ایران هستی. " +
                "نام تو 'استاد دانشمند' است. سوال علم تجربی دانش‌آموز را به زبان مادری فارسی، با روان‌ترین کلام، " +
                "تعبیر‌های ساده، فرمول‌های واضح و مثال‌های دنیای واقعی حل کن. از اموجی‌های متناسب فیزیک، شیمی و مابقی علوم " +
                "مکرراً برای زیبایی و یادگیری عمیق استفاده کن. صبوری نشان بده اگر از سوالات تکراری است."

        // Standard model check guidance
        if (key.isEmpty() || key == "MY_GEMINI_API_KEY") {
            // Local fallback simulation if API Key is not set
            return@withContext getOfflineTeacherResponse(prompt)
        }

        try {
            // Prepare request
            val currentChatHistory = _chatMessages.value
            val apiContents = mutableListOf<Content>()
            
            // Limit history context to prevent limits
            currentChatHistory.takeLast(6).forEach { msg ->
                val apiRole = if (msg.sender == "student") "user" else "model"
                apiContents.add(
                    Content(
                        parts = listOf(Part(text = msg.text)),
                        role = apiRole
                    )
                )
            }
            
            // Add latest prompt
            apiContents.add(Content(parts = listOf(Part(text = prompt)), role = "user"))

            val request = GenerateContentRequest(
                contents = apiContents,
                systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
            )

            val serviceResponse = RetrofitClient.service.generateContent(key, request)
            val textRes = serviceResponse.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            textRes ?: "پاسخی از سرور هوشمند علوم دریافت نشد. لطفا تلاش مجدد بفرمایید."

        } catch (e: Exception) {
            Log.e("ScienceViewModel", "Gemini API error call: ${e.message}")
            // Graceful response fallback on network fault
            return@withContext getOfflineTeacherResponse(prompt)
        }
    }

    /**
     * Generates extremely realistic Farsi education feedback if API key is in sandbox
        or has network limits, ensuring complete robustness.
     */
    private fun getOfflineTeacherResponse(prompt: String): String {
        val query = prompt.lowercase()
        return when {
            query.contains("اتم") || query.contains("شیمی") || query.contains("عنصر") -> {
                "آفرین به این ذوق علمی! 🧪 اتم همان سنگ بنای تمام مواد جهان است. تصورش کن مثل یک جهان مینیاتوری که درون آن پروتون‌های باردار مثبت و نوترون‌های سنگین دست دوستی به هم داده‌اند و درون هسته نشسته‌اند و الکترون‌های شتابان با بار منفی دورشان می‌چرخند.\n\n💡 یک نکته طلایی ملوکلی: فداکاری پروتون‌ها است که شناسنامه اتم را تعیین می‌کند و به آن 'عدد اتمی' معروف است که در فرمول شیمی خیلی باهاش کار داریم. دوست داری شبیه‌ساز اتم سه‌بعدی را در تب آزمایشگاه همین الان امتحانی کنی؟ 😍"
            }
            query.contains("نور") || query.contains("منشور") || query.contains("شکست") || query.contains("فیزیک")-> {
                "وای، چه سوال روشنی! 🌈 نور با وجود سادگی، رفتارهای غریبی دارد. وقتی باران نور سفید وارد دنیای غلیظ شیشه منشور می‌شود سرعت هر رنگ متفاوت کاهش می‌یابد.\n\n🔴 نور قرمز به سختی جهت خود را گم می‌کنند در حالی که این نوارهای بنفش شدیداً خمیده می‌شوند! همین زاویه خروج متفاوت باعث فروپاشی نور خورشید به طیف گرانبهای هفت رنگ رنگین کمان می‌شود.\n\n📐 پیشنهاد می‌کنم بروی تب آزمایشگاه ۳بعدی و اهرم شبیه‌ساز منشور را بچرخانی تا خم شدن فتون‌های سرخ و لاجوردی را با چشمت ببینی!"
            }
            query.contains("آتشفشان") || query.contains("واکنش") || query.contains("اسید") -> {
                "به‌به! واکنش خارق‌العاده خنثی‌سازی اسید و باز! 🔥 سرکه (استیک اسید) تشنه گرفتن یون‌های قلیایی جوش شیرین (سدیم بی‌کربنات) است.\n\n💥 با برخورد اول، یک طوفان آزادکننده گاز دی‌اکسید کربن (CO2) پدید می‌آید. این پف کردن سریع حباب‌ها همان کف‌های دوست‌داشتنی آتشفشان ما هستند!\n\n💡 فرمول ماجرا:\nCH3COOH + NaHCO3 -> CH3COONa + H2O + CO2"
            }
            query.contains("مدار") || query.contains("الکتریسیته") || query.contains("جریان") -> {
                "سوالی پُر از انرژی الکتریکی! ⚡ جریان الکتریکی درست مثل عبور آب در لوله‌ها جریان دارد!\n\n1️⃣ **ولتاژ**: فشار پمپی است که الکترون‌ها را پیش می‌راند.\n2️⃣ **مقاومت**: تنگی لوله‌ها است که جلوی عبور سریع را می‌گیرد.\n\n💡 قانون معروف اهم می‌گه:\n جریان (I) = ولتاژ (V) / مقاومت (R) \nهر چقدر مقاومت اهمی بزرگتر بشه لامپ ما کم نورتر میشه چون جریان در سختیه!"
            }
            query.contains("سلام") || query.contains("درود") -> {
                "سلام دانشمند کوشا و باهوش من! 👋 خوشحالم می‌بینمت. امروز تصمیم داری چه موضوعی از علوم تجربی رو با هم کشف کنیم؟ من کلام به کلام اینجا در خدمتتم!"
            }
            else -> {
                "به‌نظر می‌رسد به یک مبحث فوق‌العاده جدید و پویا از علوم تجربی برخورده‌ایم! 📖🤩 این موضوع نشان می‌دهد ذهن تو چقدر به فراتر از کتاب فرار کرده است.\n\nمن به عنوان معلمت 'استاد دانشمند'، پیشنهاد می‌کنم این دو کار جالب را بکنیم تا این مفهوم کاملاً ملکه ذهنت شود:\n1️⃣ به تب **کانال‌ها و ویدیوها** برو و کلیپ‌های تجربی مربوط به این موضوع را ببین.\n2️⃣ بیایم با یک آزمایش ذهنی یا سوال ساده‌تر شروع کنیم: چه عاملی در این قضیه بیشتر ذهن تو را مشغول کرده است؟"
            }
        }
    }

    // --- Curated Video Channels State ---
    val videoChannels = listOf(
        "کانال رسمی انجمن علوم تجربی",
        "آزمایشگاه ملی دانش‌آموزی",
        "مستند پدیده‌های شگفت زمین"
    )

    private val _videoList = MutableStateFlow(
        listOf(
            AcademicVideo(
                id = "vid_1",
                title = "مستند جامع غول‌های اتمی و مدل بور",
                channelName = "آزمایشگاه ملی دانش‌آموزی",
                duration = "۱۲:۴۵",
                views = "۳.۲ هزار بازدید",
                category = "شیمی",
                description = "سفری به اعماق ذرات زیراتمی و نحوه جادوی پایداری هسته با نوترون‌ها به صورت گرافیکی زنده."
            ),
            AcademicVideo(
                id = "vid_2",
                title = "شکست پرتوهای خورشید در کویر",
                channelName = "مستند پدیده‌های شگفت زمین",
                duration = "۰۸:۲۰",
                views = "۱.۹ هزار بازدید",
                category = "فیزیک",
                description = "بررسی سراب‌ها و انعکاس شدید پرتوهای نوری روی الگوهای همرفت هوای داغ کویر ایران."
            ),
            AcademicVideo(
                id = "vid_3",
                title = "آزمایش خنثی‌سازی با شناساگرهای طبیعی",
                channelName = "کانال رسمی انجمن علوم تجربی",
                duration = "۱۵:۱۰",
                views = "۵.۸ هزار بازدید",
                category = "شیمی",
                description = "با آب کلم قرمز اسید و بازهای خانگی را به رنگین‌کمان محلول‌ها تبدیل کنید و پی‌هاش سنجی یاد بگیرید."
            ),
            AcademicVideo(
                id = "vid_4",
                title = "راز کروموزوم‌ها و مهندسی دوقلوها",
                channelName = "کانال رسمی انجمن علوم تجربی",
                duration = "۱۹:۴۰",
                views = "۴.۵ هزار بازدید",
                category = "زیست‌شناسی",
                description = "کشف گام‌به‌گام ژنتیک مندلی و نحوه بسته‌بندی متراکم کلاف‌های کروموزومی در جانداران پیشرفته."
            )
        )
    )
    val videoList: StateFlow<List<AcademicVideo>> = _videoList.asStateFlow()

    // Video Player Simulation State
    private val _activeVideoForPlayback = MutableStateFlow<AcademicVideo?>(null)
    val activeVideoForPlayback: StateFlow<AcademicVideo?> = _activeVideoForPlayback.asStateFlow()

    var isPlaying = mutableStateOf(false)
    var videoProgress = mutableStateOf(0.0f) // 0f to 1.0f
    var videoResolution = mutableStateOf("1080p Ultra HD")

    fun selectVideoForPlayback(video: AcademicVideo?) {
        _activeVideoForPlayback.value = video
        isPlaying.value = (video != null)
        videoProgress.value = 0.0f
    }
}
