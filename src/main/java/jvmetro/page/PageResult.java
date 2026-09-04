package jvmetro.page;

public sealed interface PageResult
    permits PageResult.Back, PageResult.Next, PageResult.Replace, PageResult.Exit, PageResult.Stay {
  record Next(Page page) implements PageResult {
  }

  record Back() implements PageResult {
  }

  record Replace(Page page) implements PageResult {
  }

  record Exit() implements PageResult {
  }

  record Stay() implements PageResult {
  }
}
