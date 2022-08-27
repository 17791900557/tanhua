package com.tabhua.model.vo;

import com.tabhua.model.domain.BasePojo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementVo implements Serializable {
  private String id;
  private String title;
  private String description;
  private String createDate;
  private String updated;

}
