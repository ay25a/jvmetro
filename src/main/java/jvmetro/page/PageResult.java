package jvmetro.page;

public sealed interface PageResult
    permits PageResult.Back, PageResult.Next, PageResult.Exit, PageResult.Stay {
  record Next(Page page) implements PageResult {
  }

  record Back() implements PageResult {
  }

  record Exit() implements PageResult {
  }

  record Stay() implements PageResult {
  }
}
