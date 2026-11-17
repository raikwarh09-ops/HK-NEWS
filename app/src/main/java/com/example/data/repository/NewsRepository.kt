package com.example.data.repository

import com.example.data.db.NewsProjectDao
import com.example.data.model.HindiHeadlineTemplate
import com.example.data.model.HindiTeleprompterPreset
import com.example.data.model.NewsProject
import kotlinx.coroutines.flow.Flow

class NewsRepository(private val dao: NewsProjectDao) {

    val allProjects: Flow<List<NewsProject>> = dao.getAllProjects()

    suspend fun getProjectById(id: Long): NewsProject? = dao.getProjectById(id)

    suspend fun saveProject(project: NewsProject): Long = dao.insertProject(project)

    suspend fun deleteProject(project: NewsProject) = dao.deleteProject(project)

    suspend fun deleteProjectById(id: Long) = dao.deleteProjectById(id)

    fun getHeadlineTemplates(): List<HindiHeadlineTemplate> = listOf(
        HindiHeadlineTemplate(
            category = "बड़ी ख़बर",
            headline = "बड़ा प्रशासनिक फेरबदल: वरिष्ठ अधिकारियों के तबादले",
            subHeadline = "शासन ने तत्काल प्रभाव से जारी किए आदेश, नए दायित्व सौंपे",
            defaultTicker = "ताज़ा ख़बर: प्रशासनिक स्तर पर व्यापक फेरबदल • कई जिलों के अधिकारियों को नई जिम्मेदारियां सौंपी गईं • शासन ने दिए सख्त दिशा-निर्देश"
        ),
        HindiHeadlineTemplate(
            category = "ब्रेकिंग न्यूज़",
            headline = "मौसम विभाग की भारी बारिश की चेतावनी: कई जिलों में रेड अलर्ट",
            subHeadline = "प्रशासन ने जारी की एडवाइजरी, स्कूल बंद रखने के दिए निर्देश",
            defaultTicker = "मौसम अलर्ट: अगले 24 घंटों में भारी से बहुत भारी बारिश की संभावना • नदियों का जलस्तर बढ़ा, राहत दल तैनात • आवश्यक होने पर ही घर से निकलें"
        ),
        HindiHeadlineTemplate(
            category = "विशेष ख़बर",
            headline = "हाईवे पर बड़ा हादसा टला: सूझबूझ से बचीं सैकड़ों जानें",
            subHeadline = "ड्राइवर ने दिखाई हिम्मत, यात्रियों ने ली राहत की सांस",
            defaultTicker = "हादसा टला: नेशनल हाईवे पर बस का टायर फटने के बाद चालक ने सूझबूझ से बस को रोका • सभी यात्री सुरक्षित • स्थानीय लोगों ने की मदद"
        ),
        HindiHeadlineTemplate(
            category = "ग्राउंड रिपोर्ट",
            headline = "सड़कों की खस्ता हालत पर फूटा जनता का गुस्सा, सड़क जाम",
            subHeadline = "सालों से अधर में लटका निर्माण कार्य, ग्रामीणों ने दी चेतावनी",
            defaultTicker = "ग्राउंड रिपोर्ट: मुख्य मार्ग पर भारी गड्ढों से आवागमन बाधित • स्थानीय नागरिकों का प्रदर्शन • अधिकारियों ने दिया 15 दिन में समाधान का आश्वासन"
        ),
        HindiHeadlineTemplate(
            category = "राजनीति",
            headline = "विधानसभा चुनाव को लेकर राजनीतिक सरगर्मी तेज, प्रचार चरम पर",
            subHeadline = "शीर्ष नेताओं के धुआंधार दौरे शुरू, जनता से किए जा रहे बड़े वादे",
            defaultTicker = "सियासी घमासान: सभी प्रमुख दलों ने झोंकी पूरी ताकत • जनसभाओं में उमड़ी भारी भीड़ • मतदाताओं में खासा उत्साह देखने को मिला"
        ),
        HindiHeadlineTemplate(
            category = "सुपरफास्ट 100",
            headline = "देश और दुनिया की 100 बड़ी ख़बरें: फटाफट अंदाज़ में",
            subHeadline = "हर बड़ी घटना पर हमारी पैनी नज़र, पल-पल की ताज़ा अपडेट",
            defaultTicker = "सुपरफास्ट न्यूज़: विकास कार्यों की समीक्षा बैठक संपन्न • किसानों के खाते में सम्मान निधि की नई किस्त जारी • खेल जगत से आई शानदार ख़बर"
        ),
        HindiHeadlineTemplate(
            category = "खेल जगत",
            headline = "रोमांचक मुकाबले में भारत की ऐतिहासिक जीत, जश्न का माहौल",
            subHeadline = "अंतिम गेंद पर ठोका विजयी शॉट, फैंस ने फोड़े पटाखे",
            defaultTicker = "क्रिकेट स्पेशल: भारतीय टीम का शानदार प्रदर्शन • कप्तान की धमाकेदार पारी से रचा इतिहास • देश भर में जश्न का माहौल"
        ),
        HindiHeadlineTemplate(
            category = "शिक्षा व रोज़गार",
            headline = "नई भर्ती परीक्षा की तिथियां घोषित, लाखों अभ्यर्थियों को राहत",
            subHeadline = "आयोग की आधिकारिक वेबसाइट पर जारी हुआ पूरा शेड्यूल",
            defaultTicker = "रोज़गार समाचार: आगामी माह से शुरू होंगी परीक्षाएं • प्रवेश पत्र जल्द होंगे डाउनलोड • परीक्षा केंद्रों पर कड़ी सुरक्षा व्यवस्था"
        )
    )

    fun getTeleprompterPresets(): List<HindiTeleprompterPreset> = listOf(
        HindiTeleprompterPreset(
            title = "मुख्य बुलेटिन एंकर स्क्रिप्ट",
            category = "समाचार बुलेटिन",
            script = "नमस्कार! आप देख रहे हैं भारत 24, और मैं हूँ आपका संवाददाता। आज की सबसे बड़ी ख़बर के साथ बुलेटिन की शुरुआत करते हैं। राजधानी से आ रही बड़ी जानकारी के अनुसार, शासन ने आज एक महत्वपूर्ण नीतिगत फैसला लिया है। इस फैसले से आम नागरिकों को सीधा लाभ मिलेगा। जमीनी स्तर पर स्थिति का जायजा लेने के लिए हमारे विशेष संवाददाता मौके पर मौजूद हैं। आइए सीधे चलते हैं ग्राउंड ज़ीरो पर।"
        ),
        HindiTeleprompterPreset(
            title = "ग्राउंड रिपोर्टिंग लाइव स्क्रिप्ट",
            category = "ग्राउंड रिपोर्ट",
            script = "जी हाँ, इस वक्त मैं मौजूद हूँ सीधे घटनास्थल पर। मेरे पीछे आप देख सकते हैं कि किस तरह यहाँ लोगों की भारी भीड़ जुटी हुई है। प्रशासन की टीमें मौके पर पहुंच चुकी हैं और स्थिति को पूरी तरह नियंत्रण में ले लिया गया है। स्थानीय नागरिकों का कहना है कि यह समस्या पिछले कई हफ़्तों से बनी हुई थी। अब देखना होगा कि अधिकारी इस पर कितनी जल्दी ठोस कदम उठाते हैं। कैमरा पर्सन के साथ, मैं राजेश शर्मा, भारत 24।"
        ),
        HindiTeleprompterPreset(
            title = "ब्रेकिंग न्यूज़ त्वरित स्क्रिप्ट",
            category = "ब्रेकिंग",
            script = "इस वक्त की सबसे बड़ी ख़बर! दर्शकों, सीधा रुख करते हैं ताज़ा अपडेट की ओर। सूत्रों के हवाले से बहुत बड़ी ख़बर निकलकर सामने आ रही है। थोड़ी देर पहले ही उच्च स्तरीय बैठक समाप्त हुई है, जिसमें कुछ बेहद कड़े और ऐतिहासिक फैसले लिए गए हैं। आधिकारिक घोषणा बस कुछ ही मिनटों में होने की उम्मीद है। जुड़े रहिए हमारे साथ, पल-पल की सटीक जानकारी सबसे पहले आप तक।"
        )
    )
}
