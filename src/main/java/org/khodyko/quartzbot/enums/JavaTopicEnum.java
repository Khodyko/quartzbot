package org.khodyko.quartzbot.enums;

public enum JavaTopicEnum {
    CORE("Java core"),
    HIBERNATE("Hibernate"),
    SPRING("Spring");
    private String nameOfTopic;

    JavaTopicEnum(String nameOfTopic) {
        this.nameOfTopic = nameOfTopic;
    }

    public String getNameOfTopic() {
        return nameOfTopic;
    }

    public void setNameOfTopic(String nameOfTopic) {
        this.nameOfTopic = nameOfTopic;
    }

    public static JavaTopicEnum findByString(String topicName) {
        for (JavaTopicEnum topic : JavaTopicEnum.values()) {
            if (topic.getNameOfTopic().equalsIgnoreCase(topicName)) {
                return topic;
            }
        }
        return null; // Return null if no match is found
    }

    public static String toStringTopicNames() {
        StringBuilder topicNames = new StringBuilder();
        for (JavaTopicEnum topic : JavaTopicEnum.values()) {
            topicNames.append(topic.getNameOfTopic()).append(", ");
        }
        // Remove the last comma and space if there are topics
        if (topicNames.length() > 0) {
            topicNames.setLength(topicNames.length() - 2);
        }
        return topicNames.toString();
    }

    @Override
    public String toString() {
        return "JavaTopicEnum{" +
                "nameOfTopic='" + nameOfTopic + '\'' +
                '}';
    }
}
