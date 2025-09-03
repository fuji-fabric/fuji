package io.github.sakurawald.fuji.core.event.inject.structure;

import java.util.ArrayList;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class EventProducerInfoList extends ArrayList<EventProducerInfo> {

}
