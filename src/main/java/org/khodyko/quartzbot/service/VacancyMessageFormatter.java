package org.khodyko.quartzbot.service;

import org.khodyko.quartzbot.dto.hh.VacancyItemDto;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * Сервис для форматирования сообщений о вакансиях для Telegram
 */
@Service
public class VacancyMessageFormatter {

    private static final int MAX_DESCRIPTION_LENGTH = 800;
    private static final int MAX_MESSAGE_LENGTH = 4096;
    private static final String DEFAULT_FLAG = "📍";

    private static final Map<String, String> AREA_FLAG_MAP = new HashMap<>();

    static {
        // СНГ и Восточная Европа
        AREA_FLAG_MAP.put("113", "🇷🇺"); // Россия
        AREA_FLAG_MAP.put("16", "🇧🇾"); // Беларусь
        AREA_FLAG_MAP.put("40", "🇰🇿"); // Казахстан
        AREA_FLAG_MAP.put("5", "🇺🇦"); // Украина
        AREA_FLAG_MAP.put("9", "🇦🇿"); // Азербайджан
        AREA_FLAG_MAP.put("11", "🇦🇲"); // Армения
        AREA_FLAG_MAP.put("28", "🇬🇪"); // Грузия
        AREA_FLAG_MAP.put("115", "🇰🇬"); // Кыргызстан
        AREA_FLAG_MAP.put("174", "🇲🇩"); // Молдова
        AREA_FLAG_MAP.put("172", "🇹🇯"); // Таджикистан
        AREA_FLAG_MAP.put("173", "🇹🇲"); // Туркменистан
        AREA_FLAG_MAP.put("99", "🇺🇿"); // Узбекистан

        // Европа
        AREA_FLAG_MAP.put("104", "🇵🇱"); // Польша
        AREA_FLAG_MAP.put("100", "🇩🇪"); // Германия
        AREA_FLAG_MAP.put("97", "🇬🇧"); // Великобритания
        AREA_FLAG_MAP.put("101", "🇫🇷"); // Франция
        AREA_FLAG_MAP.put("102", "🇪🇸"); // Испания
        AREA_FLAG_MAP.put("103", "🇮🇹"); // Италия
        AREA_FLAG_MAP.put("105", "🇳🇱"); // Нидерланды
        AREA_FLAG_MAP.put("106", "🇨🇿"); // Чехия

        // Азия
        AREA_FLAG_MAP.put("159", "🇨🇳"); // Китай
        AREA_FLAG_MAP.put("160", "🇯🇵"); // Япония
        AREA_FLAG_MAP.put("161", "🇰🇷"); // Южная Корея
        AREA_FLAG_MAP.put("162", "🇮🇳"); // Индия
        AREA_FLAG_MAP.put("163", "🇸🇬"); // Сингапур
        AREA_FLAG_MAP.put("164", "🇹🇭"); // Таиланд
        AREA_FLAG_MAP.put("165", "🇻🇳"); // Вьетнам
        AREA_FLAG_MAP.put("166", "🇵🇭"); // Филиппины

        // Америка
        AREA_FLAG_MAP.put("84", "🇺🇸"); // США
        AREA_FLAG_MAP.put("85", "🇨🇦"); // Канада
        AREA_FLAG_MAP.put("86", "🇧🇷"); // Бразилия
        AREA_FLAG_MAP.put("87", "🇲🇽"); // Мексика
        AREA_FLAG_MAP.put("88", "🇦🇷"); // Аргентина
    }

    /**
     * Получает флаг эмодзи по ID региона
     *
     * @param areaId ID региона
     * @return флаг эмодзи или дефолтный символ
     */
    public String getFlagByAreaId(String areaId) {
        return AREA_FLAG_MAP.getOrDefault(areaId, DEFAULT_FLAG);
    }

    /**
     * Форматирует одну вакансию в читаемый текст для Telegram
     *
     * @param vacancy вакансия для форматирования
     * @return отформатированное сообщение
     */
    public String formatVacancy(VacancyItemDto vacancy) {
        StringBuilder message = new StringBuilder();

        // Название вакансии
        message.append("💼 ").append(vacancy.getName() != null ? vacancy.getName() : "Вакансия").append("\n");

        // Регион
        if (vacancy.getArea() != null) {
            String flag = getFlagByAreaId(vacancy.getArea().getId());
            message.append(flag).append(" ");
            message.append(vacancy.getArea().getName() != null ? vacancy.getArea().getName() : "Не указан");
            message.append("\n");
        }

        // Работодатель
        if (vacancy.getEmployer() != null && vacancy.getEmployer().getName() != null) {
            message.append("🏢 ").append(vacancy.getEmployer().getName()).append("\n");
        }

        // Зарплата
        if (vacancy.getSalary() != null) {
            message.append("💰 ");
            if (vacancy.getSalary().getFrom() != null && vacancy.getSalary().getTo() != null) {
                message.append(vacancy.getSalary().getFrom())
                        .append(" - ")
                        .append(vacancy.getSalary().getTo());
            } else if (vacancy.getSalary().getFrom() != null) {
                message.append("от ").append(vacancy.getSalary().getFrom());
            } else if (vacancy.getSalary().getTo() != null) {
                message.append("до ").append(vacancy.getSalary().getTo());
            }
            if (vacancy.getSalary().getCurrency() != null) {
                message.append(" ").append(formatCurrency(vacancy.getSalary().getCurrency()));
            }
            message.append("\n");
        }

        message.append("\n");

        // Описание
        String description = buildDescription(vacancy);
        if (description != null && !description.isEmpty()) {
            message.append("📝 ").append(description).append("\n\n");
        }

        // Ссылка
        String vacancyUrl = getVacancyUrl(vacancy);
        if (vacancyUrl != null && !vacancyUrl.isEmpty()) {
            message.append("🔗 ").append(vacancyUrl);
        }

        String result = message.toString();

        // Проверка на превышение лимита Telegram (4096 символов)
        if (result.length() > MAX_MESSAGE_LENGTH) {
            // Если превышает лимит, обрезаем описание еще больше
            int maxDescLength = MAX_MESSAGE_LENGTH - (result.length() - description.length()) - 10;
            if (maxDescLength > 0) {
                description = truncateDescription(description, maxDescLength);
                message = new StringBuilder();
                message.append("💼 ").append(vacancy.getName() != null ? vacancy.getName() : "Вакансия").append("\n");
                if (vacancy.getArea() != null) {
                    String flag = getFlagByAreaId(vacancy.getArea().getId());
                    message.append(flag).append(" ").append(vacancy.getArea().getName()).append("\n");
                }
                if (vacancy.getEmployer() != null && vacancy.getEmployer().getName() != null) {
                    message.append("🏢 ").append(vacancy.getEmployer().getName()).append("\n");
                }
                if (vacancy.getSalary() != null) {
                    message.append("💰 ");
                    if (vacancy.getSalary().getFrom() != null && vacancy.getSalary().getTo() != null) {
                        message.append(vacancy.getSalary().getFrom())
                                .append(" - ")
                                .append(vacancy.getSalary().getTo());
                    } else if (vacancy.getSalary().getFrom() != null) {
                        message.append("от ").append(vacancy.getSalary().getFrom());
                    } else if (vacancy.getSalary().getTo() != null) {
                        message.append("до ").append(vacancy.getSalary().getTo());
                    }
                    if (vacancy.getSalary().getCurrency() != null) {
                        message.append(" ").append(formatCurrency(vacancy.getSalary().getCurrency()));
                    }
                    message.append("\n");
                }
                message.append("\n");
                if (description != null && !description.isEmpty()) {
                    message.append("📝 ").append(description).append("\n\n");
                }
                String vacancyAdditionalUrl = getVacancyUrl(vacancy);
                if (vacancyAdditionalUrl != null && !vacancyAdditionalUrl.isEmpty()) {
                    message.append("🔗 ").append(vacancyAdditionalUrl);
                }
                result = message.toString();
            }
        }

        return result;
    }

    /**
     * Форматирует итоговое сообщение с количеством найденных вакансий
     *
     * @param totalCount общее количество найденных вакансий
     * @param date дата поиска
     * @return отформатированное сообщение
     */
    public String formatSummary(int totalCount, String date) {
        return String.format("✅ Найдено вакансий за %s: %d", date, totalCount);
    }

    /**
     * Строит описание вакансии из snippet
     *
     * @param vacancy вакансия
     * @return описание вакансии
     */
    private String buildDescription(VacancyItemDto vacancy) {
        if (vacancy.getSnippet() == null) {
            return null;
        }

        String requirement = vacancy.getSnippet().getRequirement();
        String responsibility = vacancy.getSnippet().getResponsibility();

        StringBuilder description = new StringBuilder();

        if (requirement != null && !requirement.trim().isEmpty()) {
            description.append(requirement.trim());
        }

        if (responsibility != null && !responsibility.trim().isEmpty()) {
            if (description.length() > 0) {
                description.append(" ");
            }
            description.append(responsibility.trim());
        }

        if (description.length() == 0) {
            return null;
        }

        String result = cleanHtmlTags(description.toString());
        result = normalizeWhitespace(result);
        return truncateDescription(result, MAX_DESCRIPTION_LENGTH);
    }

    /**
     * Обрезает описание до указанной длины
     *
     * @param description описание
     * @param maxLength максимальная длина
     * @return обрезанное описание
     */
    private String truncateDescription(String description, int maxLength) {
        if (description == null || description.length() <= maxLength) {
            return description;
        }

        String truncated = description.substring(0, maxLength);
        // Обрезаем до последнего пробела, чтобы не обрезать слово
        int lastSpace = truncated.lastIndexOf(' ');
        if (lastSpace > maxLength * 0.8) {
            truncated = truncated.substring(0, lastSpace);
        }
        return truncated + "...";
    }

    /**
     * Удаляет HTML-теги из текста
     *
     * @param text текст с HTML-тегами
     * @return текст без HTML-тегов
     */
    private String cleanHtmlTags(String text) {
        if (text == null) {
            return null;
        }
        return text.replaceAll("<[^>]+>", "");
    }

    /**
     * Нормализует пробелы и переносы строк
     *
     * @param text текст для нормализации
     * @return нормализованный текст
     */
    private String normalizeWhitespace(String text) {
        if (text == null) {
            return null;
        }
        return text.replaceAll("\\s+", " ").trim();
    }

    /**
     * Форматирует валюту для отображения
     *
     * @param currency код валюты
     * @return отформатированная валюта
     */
    private String formatCurrency(String currency) {
        if (currency == null) {
            return "";
        }
        return switch (currency.toUpperCase()) {
            case "RUR", "RUB" -> "руб.";
            case "USD" -> "USD";
            case "EUR" -> "EUR";
            default -> currency;
        };
    }

    /**
     * Получает URL вакансии. Если alternateUrl не указан, формирует ссылку по ID
     *
     * @param vacancy вакансия
     * @return URL вакансии
     */
    private String getVacancyUrl(VacancyItemDto vacancy) {
        if (vacancy.getAlternateUrl() != null && !vacancy.getAlternateUrl().isEmpty()) {
            return vacancy.getAlternateUrl();
        }
        // Если alternateUrl не указан, формируем ссылку по ID
        if (vacancy.getId() != null && !vacancy.getId().isEmpty()) {
            return "https://hh.ru/vacancy/" + vacancy.getId();
        }
        return null;
    }
}

