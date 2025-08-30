package ru.practicum.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCommentDto {

    @NotBlank(message = "Comment can't be empty")
    @Size(min = 2, max = 2000, message = "Comment should be from 2 to 2000 symbols")
    private String text;
}
